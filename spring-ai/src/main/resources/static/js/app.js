document.addEventListener('DOMContentLoaded', () => {
    // Tab switching
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            tab.classList.add('active');
            document.getElementById(tab.dataset.tab).classList.add('active');
        });
    });

    // Chat
    const chatMessages = document.getElementById('chat-messages');
    const chatInput = document.getElementById('chat-input');
    const chatSend = document.getElementById('chat-send');
    const streamToggle = document.getElementById('stream-toggle');

    function addMessage(text, role) {
        const div = document.createElement('div');
        div.className = `message ${role}`;
        div.textContent = text;
        chatMessages.appendChild(div);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        return div;
    }

    chatSend.addEventListener('click', async () => {
        const message = chatInput.value.trim();
        if (!message) return;

        addMessage(message, 'user');
        chatInput.value = '';
        chatSend.disabled = true;

        if (streamToggle.checked) {
            const assistantDiv = addMessage('', 'assistant');
            try {
                const response = await fetch(`/api/chat/stream?message=${encodeURIComponent(message)}`);
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let content = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;
                    const chunk = decoder.decode(value);
                    const lines = chunk.split('\n');
                    for (const line of lines) {
                        if (line.startsWith('data:')) {
                            content += line.substring(5);
                            assistantDiv.textContent = content;
                            chatMessages.scrollTop = chatMessages.scrollHeight;
                        }
                    }
                }
            } catch (err) {
                assistantDiv.textContent = 'Error: ' + err.message;
                assistantDiv.classList.add('error');
            }
        } else {
            const loadingDiv = addMessage('Thinking...', 'assistant');
            loadingDiv.classList.add('loading');
            try {
                const response = await fetch('/api/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message })
                });
                const data = await response.json();
                loadingDiv.textContent = data.response;
                loadingDiv.classList.remove('loading');
            } catch (err) {
                loadingDiv.textContent = 'Error: ' + err.message;
                loadingDiv.classList.remove('loading');
                loadingDiv.classList.add('error');
            }
        }
        chatSend.disabled = false;
    });

    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            chatSend.click();
        }
    });

    // Summarize
    document.getElementById('summarize-send').addEventListener('click', async () => {
        const text = document.getElementById('summarize-input').value.trim();
        if (!text) return;
        const result = document.getElementById('summarize-result');
        result.textContent = 'Summarizing...';
        result.className = 'result loading';
        try {
            const response = await fetch('/api/chat/summarize', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text })
            });
            const data = await response.json();
            result.textContent = data.response;
            result.className = 'result';
        } catch (err) {
            result.textContent = 'Error: ' + err.message;
            result.className = 'result error';
        }
    });

    // Translate
    document.getElementById('translate-send').addEventListener('click', async () => {
        const text = document.getElementById('translate-input').value.trim();
        const targetLanguage = document.getElementById('translate-lang').value;
        if (!text) return;
        const result = document.getElementById('translate-result');
        result.textContent = 'Translating...';
        result.className = 'result loading';
        try {
            const response = await fetch('/api/chat/translate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text, targetLanguage })
            });
            const data = await response.json();
            result.textContent = data.response;
            result.className = 'result';
        } catch (err) {
            result.textContent = 'Error: ' + err.message;
            result.className = 'result error';
        }
    });

    // Code Analysis
    document.getElementById('code-send').addEventListener('click', async () => {
        const code = document.getElementById('code-input').value.trim();
        if (!code) return;
        const result = document.getElementById('code-result');
        result.textContent = 'Analyzing...';
        result.className = 'result loading';
        try {
            const response = await fetch('/api/chat/analyze-code', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code })
            });
            const data = await response.json();
            result.textContent = data.response;
            result.className = 'result';
        } catch (err) {
            result.textContent = 'Error: ' + err.message;
            result.className = 'result error';
        }
    });

    // Image Generation
    document.getElementById('image-send').addEventListener('click', async () => {
        const prompt = document.getElementById('image-input').value.trim();
        if (!prompt) return;
        const result = document.getElementById('image-result');
        const img = document.getElementById('generated-image');
        img.style.display = 'none';
        result.textContent = 'Generating image...';
        result.className = 'result loading';
        try {
            const response = await fetch('/api/image/generate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt })
            });
            const data = await response.json();
            result.textContent = '';
            result.className = 'result';
            img.src = data.url;
            img.style.display = 'block';
        } catch (err) {
            result.textContent = 'Error: ' + err.message;
            result.className = 'result error';
        }
    });
});
