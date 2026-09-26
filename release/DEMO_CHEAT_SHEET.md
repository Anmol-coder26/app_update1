# 🛡️ Guardian Live Hackathon Demo Cheat Sheet

## ⚡ Setup Before Demo
1. **Distribution Server**: Ensure `python -m http.server 8080` (or `release/serve.bat` / `release/serve.sh`) is running on your laptop.
2. **Network**: Ensure demo phone and presenter laptop are on the same Wi-Fi / hotspot.
3. **App Initialization**:
   - Open Guardian on the demo device.
   - Complete the fast 5-step onboarding and grant permissions (Microphone, Phone, Notifications).
   - Set preferred dialect in the Language Picker to **Hindi** (or your preferred Indic language).
   - Verify Default Screening app registration.

---

## ⏱️ Pitch & Demo Flow (3 Minutes)

### 1️⃣ The Problem (30s)
> *"India loses over ₹1,750 crore annually to cyber scams — digital arrest extortion, fake electricity cuts, and reverse UPI frauds. Vulnerable citizens and non-English speakers bear the brunt because existing tools only flag static numbers in English."*

### 2️⃣ Guardian's Solution (30s)
- **Show Home Screen**: Present the 2026 dark-first interface with live security posture indicators.
- **Show Indic Language Support**: Show 10 Indian languages (Hindi, Tamil, Telugu, Marathi, Bengali, etc.).
- **Show AI Cognitive Matrix**: Explain the 4-engine breakdown (`Urgency Coercion`, `Pretext Legitimacy`, `Financial Extraction`, `Isolation Tactic`).

### 3️⃣ Live Call Threat Detection Demo (90s)
1. Tap **Speakerphone AI** (or trigger simulated dialogue via the Instant Stage Triggers).
2. **Play Scam Audio / Dialogue (Hindi)**:
   > *"मैं इंस्पेक्टर राजेश कुमार, दिल्ली साइबर क्राइम सेल से बोल रहा हूँ। आपके आधार पर 12 फर्जी पासपोर्ट और ड्रग्स पार्सल जब्त हुआ है। आप तुरंत ₹50,000 जमानत राशि ट्रांसफर करें..."*
3. **Observe Real-Time Stream**:
   - Live Hindi speech translated to English in real time via **Bhashini Streaming STT**.
   - Cognitive analysis engines detect high urgency coercion (95%) and financial extraction (98%).
   - Risk Ring score spikes to **92% (CRITICAL FRAUD THREAT)**.
4. **Automated Interventions**:
   - TTS voice alert fires in Hindi: *"उच्च जोखिम धोखाधड़ी कॉल! अभी कॉल समाप्त करें।"*
   - UI displays vibrating shake animation on the reasoning card.
5. Tap **HANG UP / BLOCK** to demonstrate immediate threat termination and audit logging.

### 4️⃣ Fallback & Reliability (30s)
- **Zero-Downtime Resilience**: Bhashini as primary multi-dialect STT/MT with automatic **Agora RTC voice stream fallback**.
- **Offline Mode**: Local regex heuristic engine provides offline protection if Wi-Fi drops.
- **Audit & Regulatory Report**: Instant one-tap report generation forwarded to the National Cybercrime Portal (`cybercrime.gov.in`).

---

## 🛟 Stage Backup Plans
- **If Wi-Fi / Internet drops**: Use the built-in stage demo triggers (`🚨 Digital Arrest`, `🏦 SBI KYC`, `⚡ Power Cut`) in `CallRiskActivity` which run offline local simulation.
- **If Bhashini WebSocket times out**: Agora RTC voice fallback activates automatically with zero UI interruption.
- **If projector/laptop disconnects**: Scan the local QR code on any phone on the hotspot to demonstrate live.
