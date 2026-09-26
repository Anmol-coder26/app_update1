import os
import sys
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import cm
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle
)
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

# Register Unicode TrueType fonts
font_dir = "C:/Windows/Fonts"
if os.path.exists(os.path.join(font_dir, "arial.ttf")):
    pdfmetrics.registerFont(TTFont('Arial', os.path.join(font_dir, 'arial.ttf')))
    pdfmetrics.registerFont(TTFont('Arial-Bold', os.path.join(font_dir, 'arialbd.ttf')))
    pdfmetrics.registerFont(TTFont('Arial-Italic', os.path.join(font_dir, 'ariali.ttf')))
    pdfmetrics.registerFont(TTFont('CourierNew', os.path.join(font_dir, 'cour.ttf')))
    pdfmetrics.registerFont(TTFont('CourierNew-Bold', os.path.join(font_dir, 'courbd.ttf')))
    FONT_NORMAL = 'Arial'
    FONT_BOLD = 'Arial-Bold'
    FONT_ITALIC = 'Arial-Italic'
    FONT_MONO = 'CourierNew'
else:
    FONT_NORMAL = 'Helvetica'
    FONT_BOLD = 'Helvetica-Bold'
    FONT_ITALIC = 'Helvetica-Oblique'
    FONT_MONO = 'Courier'

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont(FONT_NORMAL, 8.5)
        self.setFillColor(colors.HexColor("#5F6368"))
        
        # Header (Pages 2 & 3)
        if self._pageNumber > 1:
            self.drawString(2 * cm, 28.2 * cm, "Guardian — Bhashini API Key Request")
            self.drawRightString(A4[0] - 2 * cm, 28.2 * cm, "Hackindore Innovation Challenge 1.0")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(2 * cm, 28.0 * cm, A4[0] - 2 * cm, 28.0 * cm)
            
        # Footer (All pages)
        page_text = f"Page {self._pageNumber} of {page_count}"
        self.drawCentredString(A4[0] / 2.0, 1.2 * cm, page_text)
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(2 * cm, 1.6 * cm, A4[0] - 2 * cm, 1.6 * cm)
        self.restoreState()

def generate_pdf(output_path):
    margin = 2 * cm
    doc = SimpleDocTemplate(
        output_path,
        pagesize=A4,
        leftMargin=margin,
        rightMargin=margin,
        topMargin=2.0 * cm,
        bottomMargin=2.0 * cm
    )

    styles = getSampleStyleSheet()

    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName=FONT_BOLD,
        fontSize=20,
        leading=24,
        textColor=colors.HexColor('#0D47A1'),
        alignment=1,
        spaceAfter=6
    )

    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName=FONT_NORMAL,
        fontSize=11.5,
        leading=16,
        textColor=colors.HexColor('#5F6368'),
        alignment=1,
        spaceAfter=14
    )

    h1_style = ParagraphStyle(
        'SectionH1',
        parent=styles['Normal'],
        fontName=FONT_BOLD,
        fontSize=13,
        leading=16,
        textColor=colors.HexColor('#1A73E8'),
        spaceBefore=7,
        spaceAfter=3,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'SectionH2',
        parent=styles['Normal'],
        fontName=FONT_BOLD,
        fontSize=10,
        leading=13.5,
        textColor=colors.HexColor('#1E293B'),
        spaceBefore=4,
        spaceAfter=2,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'DocBody',
        parent=styles['Normal'],
        fontName=FONT_NORMAL,
        fontSize=9.3,
        leading=13.2,
        textColor=colors.HexColor('#1E293B'),
        spaceAfter=4
    )

    bullet_style = ParagraphStyle(
        'DocBullet',
        parent=styles['Normal'],
        fontName=FONT_NORMAL,
        fontSize=9.0,
        leading=12.6,
        textColor=colors.HexColor('#1E293B'),
        leftIndent=12,
        firstLineIndent=-12,
        spaceAfter=2.0
    )

    diagram_style = ParagraphStyle(
        'DiagramStyle',
        parent=styles['Normal'],
        fontName=FONT_MONO,
        fontSize=7.6,
        leading=9.4,
        textColor=colors.HexColor('#0F172A'),
        spaceBefore=2,
        spaceAfter=2
    )

    table_header_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName=FONT_BOLD,
        fontSize=8.8,
        leading=11,
        textColor=colors.HexColor('#0D47A1')
    )

    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName=FONT_NORMAL,
        fontSize=8.5,
        leading=11,
        textColor=colors.HexColor('#1E293B')
    )

    story = []

    # ==========================================
    # PAGE 1 — Cover + Executive Summary
    # ==========================================
    story.append(Spacer(1, 15))
    story.append(Paragraph("Guardian: Real-Time AI Scam Protection for India's Next Billion Users", title_style))
    story.append(Paragraph("<b>Bhashini API Key Request</b><br/>Hackindore Submission — Bhashini Domain Innovation Challenge 1.0", subtitle_style))
    story.append(Spacer(1, 10))

    exec_summary_text = (
        "<b>Guardian</b> is a multilingual, AI-powered scam protection app for Indian mobile users. "
        "It protects users across the entire scam kill chain — SMS, WhatsApp notifications, live phone calls, "
        "and UPI payment moments — and warns them in their own language, in real time, before money leaves their account.<br/><br/>"
        "Built on Bhashini's language AI stack (Streaming ASR, Translation, TTS) and Google's Gemini 1.5 Flash for "
        "zero-shot scam reasoning, Guardian is designed for India's linguistic reality: Hindi, English, Hinglish, "
        "and 22 scheduled languages.<br/><br/>"
        "We are requesting a Bhashini API key to power real-time Indian-language transcription, bidirectional translation, "
        "and voice warnings for low-literacy users."
    )

    exec_table_data = [[
        Paragraph(f"<font color='#0D47A1'><b>EXECUTIVE SUMMARY</b></font><br/><br/>{exec_summary_text}", body_style)
    ]]
    exec_table = Table(exec_table_data, colWidths=[17.0 * cm])
    exec_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor('#F0F7FF')),
        ('BOX', (0, 0), (-1, -1), 1.0, colors.HexColor('#B8D5FF')),
        ('PADDING', (0, 0), (-1, -1), 12),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ]))
    story.append(exec_table)
    story.append(Spacer(1, 14))

    story.append(Paragraph("Core Project Mission & Architectural Overview", h1_style))
    story.append(Paragraph(
        "Digital financial fraud in India increasingly exploits linguistic diversity and psychological coercion. "
        "Guardian bridges the gap between state-of-the-art foundation AI models and vernacular speakers across rural and urban India.",
        body_style
    ))
    story.append(Spacer(1, 8))

    overview_data = [
        [Paragraph("<b>Pillar</b>", table_header_style), Paragraph("<b>Specification & Technical Implementation</b>", table_header_style)],
        [Paragraph("<b>Target Audience</b>", table_cell_style), Paragraph("Elderly, rural, first-time smartphone users, and regional language speakers.", table_cell_style)],
        [Paragraph("<b>Attack Surfaces</b>", table_cell_style), Paragraph("Inbound phone calls (digital arrest, KYC fraud), SMS/WhatsApp phishing, QR codes, & UPI triggers.", table_cell_style)],
        [Paragraph("<b>Language Stack</b>", table_cell_style), Paragraph("Bhashini IndicTrans2, Streaming ASR (VAD WebSocket), and Indic TTS.", table_cell_style)],
        [Paragraph("<b>AI Reasoning</b>", table_cell_style), Paragraph("Gemini 1.5 Flash (five-engine cognitive model) with pure Kotlin offline keyword fallback.", table_cell_style)],
        [Paragraph("<b>Interception Method</b>", table_cell_style), Paragraph("Default Browser role, NotificationListenerService, Speakerphone ASR listener, & Floating HUD.", table_cell_style)],
    ]
    overview_table = Table(overview_data, colWidths=[4.2 * cm, 12.8 * cm])
    overview_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#E8F0FE')),
        ('BOX', (0, 0), (-1, -1), 0.5, colors.HexColor('#CBD5E1')),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor('#E2E8F0')),
        ('PADDING', (0, 0), (-1, -1), 6),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ]))
    story.append(overview_table)

    story.append(PageBreak())

    # ==========================================
    # PAGE 2 — Problem, Solution, Bhashini Integration
    # ==========================================
    story.append(Paragraph("The Problem", h1_style))
    story.append(Paragraph(
        "India loses over ₹1,750 crore to cyber fraud every year. The majority of victims are elderly, rural, "
        "and non-English speaking. Scams arrive through SMS, WhatsApp, phone calls, and UPI — and AI-generated voices "
        "make them harder to detect.",
        body_style
    ))
    story.append(Paragraph("<b>Existing solutions fail:</b>", h2_style))
    story.append(Paragraph("• <b>Truecaller:</b> only flags known numbers; misses new scam scripts", bullet_style))
    story.append(Paragraph("• <b>Carrier filters:</b> only detect bulk SMS; miss targeted attacks", bullet_style))
    story.append(Paragraph("• <b>Bank warnings:</b> arrive after money is gone", bullet_style))
    story.append(Paragraph("• <b>Google Safe Browsing:</b> only covers links, not calls or SMS", bullet_style))
    story.append(Paragraph("<b>The gap:</b> No existing tool protects Indian users in their own language across all scam channels in real time.", body_style))

    story.append(Spacer(1, 3))
    story.append(Paragraph("The Solution", h1_style))
    story.append(Paragraph("Guardian runs silently on the user's phone and warns them before they act.", body_style))
    story.append(Paragraph("<b>Multi-Channel Protection:</b>", h2_style))
    story.append(Paragraph("• <b>SMS / WhatsApp</b> — NotificationListenerService reads messages before user taps", bullet_style))
    story.append(Paragraph("• <b>Phone Calls</b> — Live call audio transcribed via Bhashini ASR", bullet_style))
    story.append(Paragraph("• <b>QR Codes</b> — CameraX + ML Kit scans before payment", bullet_style))
    story.append(Paragraph("• <b>Links</b> — Intercepts via default browser role", bullet_style))
    story.append(Paragraph("• <b>UPI Moment</b> — Warns if user opens a payment app during a flagged call", bullet_style))

    story.append(Spacer(1, 3))
    story.append(Paragraph("<b>The Bhashini Pipeline:</b>", h2_style))
    
    pipeline_lines = [
        "User speaks (Hindi / Hinglish / English)",
        "        ↓",
        "Bhashini Streaming ASR (WebSocket, VAD-enabled)",
        "        ↓",
        "Live transcript in user's language",
        "        ↓",
        "Bhashini Translation → English",
        "        ↓",
        "Gemini 1.5 Flash — five-engine semantic analysis",
        "        ↓",
        "RiskReport with explanation_en + explanation_hi",
        "        ↓",
        "Bhashini Translation → back to user's language",
        "        ↓",
        "Floating AI Reasoning Card (in user's language)"
    ]
    pipeline_text = "<br/>".join(pipeline_lines)
    pipeline_table_data = [[Paragraph(f"<font face='{FONT_MONO}' size='7.5'>{pipeline_text}</font>", diagram_style)]]
    pipeline_table = Table(pipeline_table_data, colWidths=[17.0 * cm])
    pipeline_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor('#F8FAFC')),
        ('BOX', (0, 0), (-1, -1), 0.5, colors.HexColor('#94A3B8')),
        ('PADDING', (0, 0), (-1, -1), 4),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
    ]))
    story.append(pipeline_table)
    story.append(Paragraph("<i>This pipeline lets an English-trained LLM reason about Hindi speech and delivers the explanation back in the user's mother tongue.</i>", ParagraphStyle('SubItalic', parent=body_style, fontName=FONT_ITALIC, fontSize=8.5, textColor=colors.HexColor('#475569'), spaceBefore=2)))

    story.append(Spacer(1, 3))
    story.append(Paragraph("<b>Five-Engine Semantic AI:</b>", h2_style))
    story.append(Paragraph("1. <b>Pretext Legitimacy</b> — Is the caller faking a bank / police / courier identity?", bullet_style))
    story.append(Paragraph("2. <b>Intent & Action Risk</b> — Are they trying to extract OTP, PIN, or funds?", bullet_style))
    story.append(Paragraph("3. <b>Psychological Pressure</b> — Are they manufacturing fear or urgency?", bullet_style))
    story.append(Paragraph("4. <b>Information Asymmetry</b> — Are they demanding secrets while giving vague claims?", bullet_style))
    story.append(Paragraph("5. <b>Zero-Shot Variant Detection</b> — Catches new scam scripts never seen before", bullet_style))

    story.append(Spacer(1, 3))
    story.append(Paragraph("<b>The AI Reasoning Card:</b> The user sees a live floating overlay during the call: Risk score (0–100%) with color gradient, signal breakdown, tactical explanation in Hindi/English, and highlighted risky phrases.", body_style))
    story.append(Paragraph("<b>Family Protection Layer:</b> When a scam is detected, family members receive a push notification with caller details and a 'Call them now' button. One-tap reporting to cybercrime.gov.in is included.", body_style))

    story.append(PageBreak())

    # ==========================================
    # PAGE 3 — Bhashini APIs, Stack, Impact, Contact
    # ==========================================
    story.append(Paragraph("Bhashini APIs We Will Use", h1_style))
    story.append(Paragraph("• <b>Streaming ASR (WebSocket)</b> — Real-time transcription in Hindi, English, Tamil, Telugu, Bengali, and 19 other languages with built-in VAD and code-mixed speech support", bullet_style))
    story.append(Paragraph("• <b>Translation API (IndicTrans2)</b> — Bidirectional translation: user language → English for LLM, English → user language for display", bullet_style))
    story.append(Paragraph("• <b>Text-to-Speech (TTS)</b> — Voice warnings for visually impaired or low-literacy users", bullet_style))

    story.append(Spacer(1, 4))
    story.append(Paragraph("Technical Stack", h1_style))
    story.append(Paragraph("• <b>Client (Android):</b> Kotlin, Jetpack Compose, CameraX, ML Kit, NotificationListenerService, Foreground Service, EncryptedSharedPreferences", bullet_style))
    story.append(Paragraph("• <b>Bhashini Integration:</b> Streaming ASR via WebSocket (OkHttp), Translation via REST (Retrofit), TTS via REST", bullet_style))
    story.append(Paragraph("• <b>AI Analysis:</b> Google Gemini 1.5 Flash (five-engine analysis), local keyword fallback for offline mode", bullet_style))
    story.append(Paragraph("• <b>Backend:</b> Node.js + Express, Firebase Cloud Messaging for family alerts", bullet_style))

    story.append(Spacer(1, 4))
    story.append(Paragraph("Why Bhashini Is Essential", h1_style))
    story.append(Paragraph(
        "Without Bhashini, Guardian would work only in English — and English-only scam protection is useless for the majority "
        "of Indians who are targeted. Bhashini's ASR, Translation, and TTS infrastructure is what turns Guardian from an English "
        "app into a genuinely national product.",
        body_style
    ))
    story.append(Paragraph("<b>Running on India's public digital infrastructure means:</b>", h2_style))
    story.append(Paragraph("• Data stays in India", bullet_style))
    story.append(Paragraph("• Free developer tier makes it accessible to small teams", bullet_style))
    story.append(Paragraph("• Directly advances the National Language Translation Mission", bullet_style))
    story.append(Paragraph("• Future-proofs against commercial vendor lock-in", bullet_style))

    story.append(Spacer(1, 4))
    story.append(Paragraph("Expected Impact", h1_style))
    impact_data = [
        [Paragraph("<b>Metric</b>", table_header_style), Paragraph("<b>Target Projection</b>", table_header_style)],
        [Paragraph("Scam events flagged per user per week", table_cell_style), Paragraph("<b>3 – 5</b> threats blocked", table_cell_style)],
        [Paragraph("Time from call start to first alert", table_cell_style), Paragraph("<b>< 3 seconds</b> (low latency)", table_cell_style)],
        [Paragraph("Flagged items confirmed as scams", table_cell_style), Paragraph("<b>> 70%</b> precision rate", table_cell_style)],
        [Paragraph("Family alerts per active circle", table_cell_style), Paragraph("<b>1 – 2</b> per month", table_cell_style)],
        [Paragraph("Cybercrime reports filed via Guardian", table_cell_style), Paragraph("<b>500+</b> in first 6 months", table_cell_style)],
    ]
    impact_table = Table(impact_data, colWidths=[9.0 * cm, 8.0 * cm])
    impact_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#E8F0FE')),
        ('BOX', (0, 0), (-1, -1), 0.5, colors.HexColor('#CBD5E1')),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor('#E2E8F0')),
        ('PADDING', (0, 0), (-1, -1), 4),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ]))
    story.append(impact_table)

    story.append(Spacer(1, 4))
    story.append(Paragraph("Honest Limitations", h1_style))
    story.append(Paragraph("• Android blocks third-party apps from directly accessing in-call audio. Guardian uses the speakerphone + microphone workaround — the same approach used by accessibility apps like Rogervoice.", bullet_style))
    story.append(Paragraph("• AI-voice detection on compressed phone audio is an unsolved problem. Guardian provides a confidence score, not a guarantee.", bullet_style))
    story.append(Paragraph("• Link interception requires setting Guardian as the default browser — an Android platform constraint.", bullet_style))

    story.append(Spacer(1, 4))
    story.append(Paragraph("About the Team & Contact", h1_style))
    story.append(Paragraph(
        "Guardian is being built for Hackindore under the <b>Bhashini Domain Innovation Challenge 1.0</b> track. "
        "The team combines Android development, AI integration, and product design experience.<br/>"
        "<b>Team:</b> Guardian AI Team &nbsp;|&nbsp; <b>Email:</b> tanyasingh1227@gmail.com &nbsp;|&nbsp; <b>Repository:</b> github.com/Anmol-coder26/app_for_hack",
        body_style
    ))

    # Build document
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"SUCCESS: Generated {output_path}")

if __name__ == "__main__":
    out_pdf = os.path.abspath(os.path.join(os.path.dirname(__file__), "Guardian_Bhashini_Submission.pdf"))
    generate_pdf(out_pdf)
