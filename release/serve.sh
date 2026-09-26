#!/bin/bash
cd "$(dirname "$0")/../app/build/outputs/apk/release/"
echo "Serving APK at:"
echo "  http://$(hostname -I | awk '{print $1}'):8080/"
echo "  Local: http://localhost:8080/"
python3 -m http.server 8080
