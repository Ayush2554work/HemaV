#!/bin/bash
# HemaV Backend — Deploy to DigitalOcean Droplet
# Usage: ./deploy.sh <droplet-ip>

set -e

DROPLET_IP="${1:-64.227.150.118}"
REMOTE_DIR="/opt/hemav-backend"

echo "🚀 Deploying HemaV Backend to $DROPLET_IP..."

# Upload backend files
echo "📦 Uploading files..."
rsync -avz --exclude='.venv' --exclude='__pycache__' --exclude='.git' \
  -e ssh ./backend/ root@$DROPLET_IP:$REMOTE_DIR/

# Run setup on server
echo "⚙️ Setting up on server..."
ssh root@$DROPLET_IP << 'EOF'
cd /opt/hemav-backend

# Install Python if needed
apt-get update -qq && apt-get install -y -qq python3 python3-pip python3-venv > /dev/null 2>&1

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -q -r requirements.txt

# Stop existing service if running
systemctl stop hemav-api 2>/dev/null || true

# Create systemd service
cat > /etc/systemd/system/hemav-api.service << 'SERVICE'
[Unit]
Description=HemaV API Backend
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/hemav-backend
Environment=PATH=/opt/hemav-backend/venv/bin:/usr/bin:/bin
EnvironmentFile=/opt/hemav-backend/.env
ExecStart=/opt/hemav-backend/venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8000
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
SERVICE

# Enable and start
systemctl daemon-reload
systemctl enable hemav-api
systemctl start hemav-api

echo "✅ HemaV API is running!"
echo "🌐 API: http://$(hostname -I | awk '{print $1}'):8000"
echo "📚 Docs: http://$(hostname -I | awk '{print $1}'):8000/docs"
EOF

echo ""
echo "✅ Deployment complete!"
echo "🌐 API: http://$DROPLET_IP:8000"
echo "📚 Swagger Docs: http://$DROPLET_IP:8000/docs"
