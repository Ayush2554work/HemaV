"""
HemaV Backend - Prescription Routes
"""
from fastapi import APIRouter, Depends, HTTPException
from app.auth import get_current_user
from app.database import prescriptions_collection, users_collection
from app.models import PrescriptionCreate
from bson import ObjectId
from datetime import datetime

router = APIRouter(prefix="/prescriptions", tags=["Prescriptions"])


@router.post("/")
async def create_prescription(
    data: PrescriptionCreate,
    current_user: dict = Depends(get_current_user),
):
    if current_user["role"] != "DOCTOR":
        raise HTTPException(status_code=403, detail="Only doctors can create prescriptions")

    doctor = await users_collection().find_one({"_id": ObjectId(current_user["user_id"])})

    doc = {
        "patient_id": data.patient_id,
        "doctor_id": current_user["user_id"],
        "doctor_name": doctor.get("name", "") if doctor else "",
        "appointment_id": data.appointment_id,
        "medicines": [m.model_dump() for m in data.medicines],
        "diagnosis": data.diagnosis,
        "notes": data.notes,
        "pdf_url": "",
        "created_at": datetime.utcnow(),
    }
    result = await prescriptions_collection().insert_one(doc)
    return {"id": str(result.inserted_id), "status": "created"}


@router.get("/")
async def list_prescriptions(current_user: dict = Depends(get_current_user)):
    role = current_user["role"]
    user_id = current_user["user_id"]

    field = "patient_id" if role == "PATIENT" else "doctor_id"
    cursor = prescriptions_collection().find({field: user_id}).sort("created_at", -1).limit(50)

    prescriptions = []
    async for doc in cursor:
        doc["id"] = str(doc["_id"])
        doc.pop("_id", None)
        prescriptions.append(doc)
    return prescriptions
