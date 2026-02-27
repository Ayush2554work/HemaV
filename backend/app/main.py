"""
HemaV Backend — Main Application Entry Point
FastAPI server with MongoDB, JWT auth, and RESTful APIs
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from app.config import get_settings
from app.database import connect_db, close_db

settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup / shutdown events"""
    await connect_db()
    yield
    await close_db()


app = FastAPI(
    title="HemaV API",
    description="AI-Powered Ayurvedic Telehealth Backend",
    version="1.0.0-poc",
    lifespan=lifespan,
)

# CORS — allow Android app to connect
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins.split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ─── Register Routes ─────────────────────────────────
from app.routes.auth import router as auth_router
from app.routes.users import router as users_router
from app.routes.appointments import router as appointments_router
from app.routes.scans import router as scans_router
from app.routes.prescriptions import router as prescriptions_router

app.include_router(auth_router)
app.include_router(users_router)
app.include_router(appointments_router)
app.include_router(scans_router)
app.include_router(prescriptions_router)


# ─── Health Check ─────────────────────────────────────
@app.get("/", tags=["Health"])
async def root():
    return {
        "app": "HemaV API",
        "version": "1.0.0-poc",
        "status": "running",
        "docs": "/docs",
    }


@app.get("/health", tags=["Health"])
async def health_check():
    from app.database import client
    try:
        await client.admin.command("ping")
        db_ok = True
    except Exception:
        db_ok = False

    return {
        "status": "ok" if db_ok else "degraded",
        "version": "1.0.0-poc",
        "db_connected": db_ok,
    }
