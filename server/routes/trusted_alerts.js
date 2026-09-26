const express = require('express');
const router = express.Router();

router.post('/trusted-contact', async (req, res) => {
    const { to, contactName, callerNumber, riskScore, signals } = req.body;

    if (!to || !callerNumber) {
        return res.status(400).json({ error: 'Missing required fields' });
    }

    const message = `⚠️ Guardian Alert\n\n${contactName || 'Family Member'}, you are listed as a trusted contact. A suspicious call was just detected on your family member's phone.\n\nCaller: ${callerNumber}\nRisk: ${riskScore}%\nSignals: ${(signals || []).join(', ')}\n\nPlease check on them immediately.`;

    try {
        console.log(`[TrustedAlert] Dispatching emergency alert to ${to}:\n${message}`);
        res.json({ success: true, provider: 'console-stub', recipient: to });
    } catch (err) {
        console.error('[TrustedAlert] Failed:', err);
        res.status(500).json({ error: 'Send failed' });
    }
});

module.exports = router;
