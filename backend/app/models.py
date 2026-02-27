"""
HemaV Backend - Pydantic Models
Mirrors the Android Kotlin data classes for API consistency
"""
from pydantic import BaseModel, Field, EmailStr
from typing import Optional, List
from enum import Enum
from datetime import datetime


# ─── Enums ────────────────────────────────────────────
class UserRole(str, Enum):
    PATIENT = "PATIENT"
    DOCTOR = "DOCTOR"


class AppointmentType(str, Enum):
    VIDEO = "VIDEO"
    IN_PERSON = "IN_PERSON"


class AppointmentStatus(str, Enum):
    PENDING = "PENDING"
    CONFIRMED = "CONFIRMED"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"


# ─── Auth ─────────────────────────────────────────────
class UserRegister(BaseModel):
    name: str
    email: EmailStr
    phone: str = ""
    password: str
    role: UserRole = UserRole.PATIENT


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: str
    role: UserRole
    name: str


# ─── User ─────────────────────────────────────────────
class UserOut(BaseModel):
    id: str = Field(alias="_id", default="")
    uid: str = ""
    name: str = ""
    email: str = ""
    phone: str = ""
    role: UserRole = UserRole.PATIENT
    profile_pic_url: str = ""
    created_at: datetime = Field(default_factory=datetime.utcnow)

    class Config:
        populate_by_name = True


# ─── Doctor Profile ───────────────────────────────────
class DoctorProfileCreate(BaseModel):
    license_number: str = ""
    specialties: List[str] = []
    qualifications: str = ""
    degree: str = ""
    experience: int = 0
    about: str = ""
    clinic_address: str = ""
    city: str = ""
    consultation_fee: float = 0.0
    available_slots: List[str] = []
    latitude: float = 0.0
    longitude: float = 0.0


class DoctorProfileOut(DoctorProfileCreate):
    uid: str = ""
    name: str = ""
    rating: float = 0.0
    total_ratings: int = 0
    is_verified: bool = False
    profile_pic_url: str = ""


# ─── Patient Profile ─────────────────────────────────
class PatientProfileUpdate(BaseModel):
    date_of_birth: str = ""
    blood_group: str = ""
    gender: str = ""
    address: str = ""
    medical_history: List[str] = []
    allergies: List[str] = []


# ─── Appointment ──────────────────────────────────────
class AppointmentCreate(BaseModel):
    doctor_id: str
    date: str
    time: str
    type: AppointmentType = AppointmentType.VIDEO
    notes: str = ""
    patient_age: str = ""
    patient_gender: str = ""
    patient_blood_group: str = ""
    patient_weight: str = ""


class AppointmentOut(BaseModel):
    id: str = ""
    patient_id: str = ""
    doctor_id: str = ""
    patient_name: str = ""
    doctor_name: str = ""
    date: str = ""
    time: str = ""
    timestamp: int = 0
    type: AppointmentType = AppointmentType.VIDEO
    status: AppointmentStatus = AppointmentStatus.PENDING
    notes: str = ""
    created_at: datetime = Field(default_factory=datetime.utcnow)


# ─── Prescription ────────────────────────────────────
class MedicineItem(BaseModel):
    name: str
    dosage: str = ""
    frequency: str = ""
    duration: str = ""
    instructions: str = ""


class PrescriptionCreate(BaseModel):
    patient_id: str
    appointment_id: str = ""
    medicines: List[MedicineItem] = []
    diagnosis: str = ""
    notes: str = ""


class PrescriptionOut(PrescriptionCreate):
    id: str = ""
    doctor_id: str = ""
    doctor_name: str = ""
    pdf_url: str = ""
    created_at: datetime = Field(default_factory=datetime.utcnow)


# ─── Scan Results ────────────────────────────────────
class ScanResultOut(BaseModel):
    id: str = ""
    user_id: str = ""
    risk_level: str = ""
    confidence: float = 0.0
    hemoglobin_estimate: str = ""
    details: str = ""
    recommendations: List[str] = []
    image_urls: List[str] = []
    created_at: datetime = Field(default_factory=datetime.utcnow)


# ─── Chat ─────────────────────────────────────────────
class MessageCreate(BaseModel):
    receiver_id: str
    text: str = ""
    media_url: str = ""
    media_type: str = ""


class MessageOut(BaseModel):
    id: str = ""
    chat_id: str = ""
    sender_id: str = ""
    receiver_id: str = ""
    sender_name: str = ""
    text: str = ""
    media_url: str = ""
    media_type: str = ""
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    is_read: bool = False


# ─── Health Check ─────────────────────────────────────
class HealthResponse(BaseModel):
    status: str = "ok"
    version: str = "1.0.0-poc"
    db_connected: bool = False
