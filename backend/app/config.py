"""
HemaV Backend - Configuration
Loads settings from environment variables / .env file
"""
from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # MongoDB
    mongodb_uri: str = "mongodb://localhost:27017"
    db_name: str = "hemav"

    # JWT Auth
    jwt_secret: str = "change-me-in-production"
    jwt_algorithm: str = "HS256"
    jwt_expiry_hours: int = 72

    # AI APIs
    gemini_api_key: str = ""
    groq_api_key: str = ""

    # CORS
    cors_origins: str = "*"

    # Sentry
    sentry_dsn: str = ""

    # Razorpay
    razorpay_key_id: str = ""
    razorpay_key_secret: str = ""

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


@lru_cache()
def get_settings() -> Settings:
    return Settings()
