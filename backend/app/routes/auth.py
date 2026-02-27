"""
HemaV Backend - Auth Routes (Register / Login)
"""
from fastapi import APIRouter, HTTPException, status
from app.models import UserRegister, UserLogin, TokenResponse
from app.auth import hash_password, verify_password, create_token
from app.database import users_collection, doctors_collection
from datetime import datetime

router = APIRouter(prefix="/auth", tags=["Authentication"])


@router.post("/register", response_model=TokenResponse)
async def register(data: UserRegister):
    coll = users_collection()

    # Check existing
    if await coll.find_one({"email": data.email}):
        raise HTTPException(status_code=400, detail="Email already registered")

    user_doc = {
        "name": data.name,
        "email": data.email,
        "phone": data.phone,
        "role": data.role.value,
        "password_hash": hash_password(data.password),
        "profile_pic_url": "",
        "created_at": datetime.utcnow(),
    }

    result = await coll.insert_one(user_doc)
    user_id = str(result.inserted_id)

    # If doctor, create empty doctor profile
    if data.role.value == "DOCTOR":
        await doctors_collection().insert_one({
            "uid": user_id,
            "name": data.name,
            "specialties": [],
            "qualifications": "",
            "experience": 0,
            "consultation_fee": 0.0,
            "rating": 0.0,
            "is_verified": False,
            "created_at": datetime.utcnow(),
        })

    token = create_token(user_id, data.role.value)
    return TokenResponse(
        access_token=token,
        user_id=user_id,
        role=data.role,
        name=data.name,
    )


@router.post("/login", response_model=TokenResponse)
async def login(data: UserLogin):
    coll = users_collection()
    user = await coll.find_one({"email": data.email})

    if not user or not verify_password(data.password, user["password_hash"]):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    user_id = str(user["_id"])
    role = user["role"]
    token = create_token(user_id, role)

    return TokenResponse(
        access_token=token,
        user_id=user_id,
        role=role,
        name=user["name"],
    )
