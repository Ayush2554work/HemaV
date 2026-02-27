"""
HemaV Backend - Scan Results Routes
"""
from fastapi import APIRouter, Depends
from app.auth import get_current_user
from app.database import scans_collection
from app.models import ScanResultOut
from datetime import datetime

router = APIRouter(prefix="/scans", tags=["Anemia Scans"])


@router.post("/")
async def save_scan_result(
    data: dict,
    current_user: dict = Depends(get_current_user),
):
    """Save anemia scan result from Android app"""
    doc = {
        "user_id": current_user["user_id"],
        "risk_level": data.get("risk_level", ""),
        "confidence": data.get("confidence", 0.0),
        "hemoglobin_estimate": data.get("hemoglobin_estimate", ""),
        "details": data.get("details", ""),
        "recommendations": data.get("recommendations", []),
        "image_urls": data.get("image_urls", []),
        "patient_details": data.get("patient_details", {}),
        "raw_analysis": data.get("raw_analysis", ""),
        "created_at": datetime.utcnow(),
    }
    result = await scans_collection().insert_one(doc)
    return {"id": str(result.inserted_id), "status": "saved"}


@router.get("/")
async def list_scans(current_user: dict = Depends(get_current_user)):
    """Get all scans for the current user"""
    cursor = scans_collection().find(
        {"user_id": current_user["user_id"]}
    ).sort("created_at", -1).limit(50)

    scans = []
    async for doc in cursor:
        doc["id"] = str(doc["_id"])
        doc.pop("_id", None)
        scans.append(doc)
    return scans


@router.get("/{scan_id}")
async def get_scan(scan_id: str, current_user: dict = Depends(get_current_user)):
    from bson import ObjectId
    doc = await scans_collection().find_one({"_id": ObjectId(scan_id)})
    if not doc:
        from fastapi import HTTPException
        raise HTTPException(status_code=404, detail="Scan not found")
    doc["id"] = str(doc["_id"])
    doc.pop("_id", None)
    return doc
