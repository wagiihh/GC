// Shared Chat Functionality with State Persistence
class ChatManager {
    constructor() {
        this.isChatOpen = false;
        this.chatHistory = [];
        this.init();
    }

    init() {
        // Load chat state from localStorage
        this.loadChatState();
        
        // Set up event listeners
        this.setupEventListeners();
        
        // Restore chat state if it was open
        if (this.isChatOpen) {
            this.toggleChat();
        }
    }

    setupEventListeners() {
        // Handle Enter key in textarea
        const chatInput = document.getElementById('chatInput');
        if (chatInput) {
            chatInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
        }
    }

    toggleChat() {
        const chatWindow = document.getElementById('chatWindow');
        const chatToggle = document.querySelector('.chat-toggle');
        
        this.isChatOpen = !this.isChatOpen;
        
        if (this.isChatOpen) {
            chatWindow.style.display = 'block';
            chatToggle.classList.add('active');
            setTimeout(() => {
                chatWindow.classList.add('open');
            }, 10);
        } else {
            chatWindow.classList.remove('open');
            setTimeout(() => {
                chatWindow.style.display = 'none';
            }, 300);
        }
        
        // Save chat state
        this.saveChatState();
    }

    sendQuickMessage(message) {
        const chatInput = document.getElementById('chatInput');
        if (chatInput) {
            chatInput.value = message;
            this.sendMessage();
        }
    }

    async sendMessage() {
        const input = document.getElementById('chatInput');
        const message = input.value.trim();
        
        if (!message) return;
        
        // Add user message to chat
        this.addMessage(message, 'user');
        input.value = '';
        
        // Show typing indicator
        this.showTypingIndicator();
        
        try {
            // Send to ChatGPT API
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    message: message,
                    context: 'photography_shoot_planning'
                })
            });
            
            const data = await response.json();
            
            // Remove typing indicator
            this.removeTypingIndicator();
            
            // Add assistant response
            this.addMessage(data.response, 'assistant');
            
        } catch (error) {
            console.error('Error:', error);
            this.removeTypingIndicator();
            this.addMessage('Sorry, I encountered an error. Please try again.', 'assistant');
        }
    }

    addMessage(text, sender) {
        const messagesContainer = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}-message`;
        
        const avatar = sender === 'assistant' ? 
            '<i class="fas fa-robot"></i>' : 
            '<i class="fas fa-user"></i>';
        
        messageDiv.innerHTML = `
            <div class="message-content">
                <div class="message-avatar">
                    ${avatar}
                </div>
                <div class="message-text">
                    <p>${text}</p>
                </div>
            </div>
        `;
        
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        
        // Save to chat history
        this.chatHistory.push({ text, sender, timestamp: Date.now() });
        this.saveChatHistory();
    }

    showTypingIndicator() {
        const messagesContainer = document.getElementById('chatMessages');
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message assistant-message typing-indicator';
        typingDiv.id = 'typingIndicator';
        
        typingDiv.innerHTML = `
            <div class="message-content">
                <div class="message-avatar">
                    <i class="fas fa-robot"></i>
                </div>
                <div class="message-text">
                    <div class="typing-dots">
                        <span></span>
                        <span></span>
                        <span></span>
                    </div>
                </div>
            </div>
        `;
        
        messagesContainer.appendChild(typingDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    removeTypingIndicator() {
        const typingIndicator = document.getElementById('typingIndicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    saveChatState() {
        localStorage.setItem('chatOpen', this.isChatOpen.toString());
    }

    loadChatState() {
        const savedState = localStorage.getItem('chatOpen');
        if (savedState !== null) {
            this.isChatOpen = savedState === 'true';
        }
    }

    saveChatHistory() {
        // Keep only last 50 messages to prevent localStorage bloat
        if (this.chatHistory.length > 50) {
            this.chatHistory = this.chatHistory.slice(-50);
        }
        localStorage.setItem('chatHistory', JSON.stringify(this.chatHistory));
    }

    loadChatHistory() {
        const savedHistory = localStorage.getItem('chatHistory');
        if (savedHistory) {
            this.chatHistory = JSON.parse(savedHistory);
            this.renderChatHistory();
        }
    }

    renderChatHistory() {
        const messagesContainer = document.getElementById('chatMessages');
        
        // Clear existing messages except the welcome message
        const welcomeMessage = messagesContainer.querySelector('.assistant-message');
        messagesContainer.innerHTML = '';
        
        if (welcomeMessage) {
            messagesContainer.appendChild(welcomeMessage);
        }
        
        // Render chat history
        this.chatHistory.forEach(message => {
            this.addMessageToDOM(message.text, message.sender);
        });
    }

    addMessageToDOM(text, sender) {
        const messagesContainer = document.getElementById('chatMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}-message`;
        
        const avatar = sender === 'assistant' ? 
            '<i class="fas fa-robot"></i>' : 
            '<i class="fas fa-user"></i>';
        
        messageDiv.innerHTML = `
            <div class="message-content">
                <div class="message-avatar">
                    ${avatar}
                </div>
                <div class="message-text">
                    <p>${text}</p>
                </div>
            </div>
        `;
        
        messagesContainer.appendChild(messageDiv);
    }

    clearChatHistory() {
        this.chatHistory = [];
        localStorage.removeItem('chatHistory');
        
        // Clear messages container except welcome message
        const messagesContainer = document.getElementById('chatMessages');
        const welcomeMessage = messagesContainer.querySelector('.assistant-message');
        messagesContainer.innerHTML = '';
        
        if (welcomeMessage) {
            messagesContainer.appendChild(welcomeMessage);
        }
    }
}

// Global chat manager instance
let chatManager;

// Initialize chat when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    chatManager = new ChatManager();
    chatManager.loadChatHistory();
});

// Global functions for HTML onclick handlers
function toggleChat() {
    if (chatManager) {
        chatManager.toggleChat();
    }
}

function sendQuickMessage(message) {
    if (chatManager) {
        chatManager.sendQuickMessage(message);
    }
}

function sendMessage() {
    if (chatManager) {
        chatManager.sendMessage();
    }
}
