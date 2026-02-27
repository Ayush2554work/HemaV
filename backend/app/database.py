"""
HemaV Backend - MongoDB Database Connection
Uses Motor (async MongoDB driver) for non-blocking DB operations
"""
from motor.motor_asyncio import AsyncIOMotorClient
from app.config import get_settings

settings = get_settings()

client: AsyncIOMotorClient = None
db = None


async def connect_db():
    """Initialize MongoDB connection on startup"""
    global client, db
    client = AsyncIOMotorClient(
        settings.mongodb_uri,
        serverSelectionTimeoutMS=5000  # Fail fast if unreachable
    )
    db = client[settings.db_name]
    print(f"✅ MongoDB client initialized for: {settings.db_name}")
    # Note: actual connection happens on first query (lazy)


async def close_db():
    """Close MongoDB connection on shutdown"""
    global client
    if client:
        client.close()
        print("🔌 MongoDB connection closed")


def get_db():
    """Get database instance"""
    return db


# Collection helpers
def users_collection():
    return db["users"]

def doctors_collection():
    return db["doctors"]

def appointments_collection():
    return db["appointments"]

def prescriptions_collection():
    return db["prescriptions"]

def scans_collection():
    return db["scans"]

def chats_collection():
    return db["chats"]

def messages_collection():
    return db["messages"]

def forum_posts_collection():
    return db["forum_posts"]
