"""
HemaV Backend - User & Profile Routes
"""
from fastapi import APIRouter, Depends, HTTPException
from app.auth import get_current_user
from app.database import users_collection, doctors_collection
from app.models import PatientProfileUpdate, DoctorProfileCreate, DoctorProfileOut
from bson import ObjectId

router = APIRouter(prefix="/users", tags=["Users"])


@router.get("/me")
async def get_profile(current_user: dict = Depends(get_current_user)):
    coll = users_collection()
    user = await coll.find_one({"_id": ObjectId(current_user["user_id"])})
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user["_id"] = str(user["_id"])
    user.pop("password_hash", None)
    return user


@router.put("/me/patient-profile")
async def update_patient_profile(
    data: PatientProfileUpdate,
    current_user: dict = Depends(get_current_user),
):
    await users_collection().update_one(
        {"_id": ObjectId(current_user["user_id"])},
        {"$set": data.model_dump()},
    )
    return {"status": "updated"}


@router.put("/me/doctor-profile")
async def update_doctor_profile(
    data: DoctorProfileCreate,
    current_user: dict = Depends(get_current_user),
):
    await doctors_collection().update_one(
        {"uid": current_user["user_id"]},
        {"$set": data.model_dump()},
        upsert=True,
    )
    return {"status": "updated"}


@router.get("/doctors", response_model=list[DoctorProfileOut])
async def list_doctors(city: str = "", specialty: str = ""):
    query = {}
    if city:
        query["city"] = {"$regex": city, "$options": "i"}
    if specialty:
        query["specialties"] = {"$in": [specialty]}

    cursor = doctors_collection().find(query).limit(50)
    doctors = []
    async for doc in cursor:
        doc["_id"] = str(doc["_id"])
        doctors.append(DoctorProfileOut(**doc))
    return doctors


@router.get("/doctors/{doctor_id}")
async def get_doctor(doctor_id: str):
    doc = await doctors_collection().find_one({"uid": doctor_id})
    if not doc:
        raise HTTPException(status_code=404, detail="Doctor not found")
    doc["_id"] = str(doc["_id"])
    return doc
