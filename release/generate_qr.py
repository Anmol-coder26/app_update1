import socket
import qrcode
import os

def get_lan_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        return s.getsockname()[0]
    finally:
        s.close()

def main():
    lan_ip = get_lan_ip()
    apk_name = "app-arm64-v8a-release.apk"
    url = f"http://{lan_ip}:8080/{apk_name}"
    qr = qrcode.QRCode(
        version=None,
        error_correction=qrcode.constants.ERROR_CORRECT_M,
        box_size=12,
        border=4,
    )
    qr.add_data(url)
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    os.makedirs("release", exist_ok=True)
    img.save("release/guardian_apk_qr.png")
    img.save(r"C:\Users\Tanya\.gemini\antigravity\brain\04c31c88-306e-4741-95e8-684554b11974\guardian_apk_qr.png")
    print(f"APK URL: {url}")
    print("QR saved to release/guardian_apk_qr.png")

if __name__ == "__main__":
    main()
