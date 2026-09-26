import os
import shutil
import socket
import qrcode

def get_lan_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        return s.getsockname()[0]
    except Exception:
        return "127.0.0.1"
    finally:
        s.close()

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    lan_ip = get_lan_ip()
    apk_name = "app-arm64-v8a-release.apk"
    apk_url = f"http://{lan_ip}:8080/{apk_name}"
    repo_url = "https://github.com/Anmol-coder26/app_update1"

    # 1. Generate APK Download QR
    qr_apk = qrcode.QRCode(
        version=None,
        error_correction=qrcode.constants.ERROR_CORRECT_M,
        box_size=12,
        border=4,
    )
    qr_apk.add_data(apk_url)
    qr_apk.make(fit=True)
    img_apk = qr_apk.make_image(fill_color="black", back_color="white")
    apk_qr_path = os.path.join(script_dir, "guardian_apk_qr.png")
    img_apk.save(apk_qr_path)
    print(f"APK URL: {apk_url}")
    print(f"APK QR saved to: {apk_qr_path}")

    # 2. Generate GitHub Repo QR
    qr_repo = qrcode.QRCode(
        version=None,
        error_correction=qrcode.constants.ERROR_CORRECT_M,
        box_size=12,
        border=4,
    )
    qr_repo.add_data(repo_url)
    qr_repo.make(fit=True)
    img_repo = qr_repo.make_image(fill_color="black", back_color="white")
    repo_qr_path = os.path.join(script_dir, "guardian_repo_qr.png")
    img_repo.save(repo_qr_path)
    print(f"Repo URL: {repo_url}")
    print(f"Repo QR saved to: {repo_qr_path}")

    # 3. Copy to Artifacts directory for UI presentation
    artifact_dir = r"C:\Users\Tanya\.gemini\antigravity\brain\04c31c88-306e-4741-95e8-684554b11974"
    if os.path.exists(artifact_dir):
        shutil.copy(apk_qr_path, os.path.join(artifact_dir, "guardian_apk_qr.png"))
        shutil.copy(repo_qr_path, os.path.join(artifact_dir, "guardian_repo_qr.png"))
        print(f"Copied QR codes to artifact directory: {artifact_dir}")

if __name__ == "__main__":
    main()
