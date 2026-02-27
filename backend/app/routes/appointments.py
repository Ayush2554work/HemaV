"""
HemaV Backend - Appointment Routes
"""
from fastapi import APIRouter, Depends, HTTPException
from app.auth import get_current_user
from app.database import appointments_collection, users_collection, doctors_collection
from app.models import AppointmentCreate, AppointmentOut, AppointmentStatus
from bson import ObjectId
from datetime import datetime

router = APIRouter(prefix="/appointments", tags=["Appointments"])


@router.post("/", response_model=AppointmentOut)
async def create_appointment(
    data: AppointmentCreate,
    current_user: dict = Depends(get_current_user),
):
    patient = await users_collection().find_one({"_id": ObjectId(current_user["user_id"])})
    doctor = await doctors_collection().find_one({"uid": data.doctor_id})

    if not patient or not doctor:
        raise HTTPException(status_code=404, detail="Patient or doctor not found")

    doc = {
        "patient_id": current_user["user_id"],
        "doctor_id": data.doctor_id,
        "patient_name": patient.get("name", ""),
        "doctor_name": doctor.get("name", ""),
        "date": data.date,
        "time": data.time,
        "type": data.type.value,
        "status": AppointmentStatus.PENDING.value,
        "notes": data.notes,
        "patient_age": data.patient_age,
        "patient_gender": data.patient_gender,
        "patient_blood_group": data.patient_blood_group,
        "patient_weight": data.patient_weight,
        "created_at": datetime.utcnow(),
    }

    result = await appointments_collection().insert_one(doc)
    doc["id"] = str(result.inserted_id)
    return AppointmentOut(**doc)


@router.get("/")
async def list_appointments(current_user: dict = Depends(get_current_user)):
    role = current_user["role"]
    user_id = current_user["user_id"]

    query = {"patient_id": user_id} if role == "PATIENT" else {"doctor_id": user_id}
    cursor = appointments_collection().find(query).sort("created_at", -1).limit(50)

    appointments = []
    async for doc in cursor:
        doc["id"] = str(doc["_id"])
        doc.pop("_id", None)
        appointments.append(doc)
    return appointments


@router.put("/{appointment_id}/status")
async def update_status(
    appointment_id: str,
    status: AppointmentStatus,
    current_user: dict = Depends(get_current_user),
):
    result = await appointments_collection().update_one(
        {"_id": ObjectId(appointment_id)},
        {"$set": {"status": status.value}},
    )
    if result.modified_count == 0:
        raise HTTPException(status_code=404, detail="Appointment not found")
    return {"status": "updated"}
