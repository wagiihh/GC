// Category Shoots Page JavaScript

// Global variables for media management
let currentMediaItems = [];
let currentIndex = 0;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Collect all media items from the page
    collectMediaItems();
    
    // Setup keyboard navigation
    document.addEventListener('keydown', handleKeyboard);
});

// Collect all media items into an array
function collectMediaItems() {
    const items = document.querySelectorAll('.media-item');
    currentMediaItems = Array.from(items).map(item => ({
        url: item.dataset.url,
        type: item.dataset.type,
        name: item.dataset.name
    }));
}

// Open preview modal
function previewMedia(url, type, name) {
    const modal = document.getElementById('previewModal');
    const previewImage = document.getElementById('previewImage');
    const previewVideo = document.getElementById('previewVideo');
    
    // Find the current index
    currentIndex = currentMediaItems.findIndex(item => item.url === url);
    
    // Show the modal
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    
    // Update preview based on type
    if (type === 'image') {
        previewImage.src = url;
        previewImage.style.display = 'block';
        previewVideo.style.display = 'none';
    } else if (type === 'video') {
        previewVideo.src = url;
        previewVideo.style.display = 'block';
        previewImage.style.display = 'none';
    }
    
    // Update info panel
    updateInfoPanel(name, type);
}

// Close modal
function closeModal() {
    const modal = document.getElementById('previewModal');
    const previewImage = document.getElementById('previewImage');
    const previewVideo = document.getElementById('previewVideo');
    
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
    
    // Pause video and clear sources
    previewVideo.pause();
    previewImage.src = '';
    previewVideo.src = '';
}

// Navigate between media items
function navigateMedia(direction) {
    if (currentMediaItems.length === 0) return;
    
    if (direction === 'next') {
        currentIndex = (currentIndex + 1) % currentMediaItems.length;
    } else if (direction === 'prev') {
        currentIndex = (currentIndex - 1 + currentMediaItems.length) % currentMediaItems.length;
    }
    
    const item = currentMediaItems[currentIndex];
    previewMedia(item.url, item.type, item.name);
}

// Download file
function downloadFile(url, name) {
    try {
        // Create a temporary anchor element
        const link = document.createElement('a');
        link.href = url;
        link.download = name || 'download';
        link.target = '_blank';
        
        // Append to body, click, and remove
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        console.log('Download started:', name);
    } catch (error) {
        console.error('Download error:', error);
        // Fallback: open in new tab
        window.open(url, '_blank');
    }
}

// Download current file in modal
function downloadCurrentFile() {
    if (currentMediaItems.length === 0 || currentIndex < 0) return;
    
    const item = currentMediaItems[currentIndex];
    downloadFile(item.url, item.name);
}

// Update info panel
function updateInfoPanel(filename, type) {
    document.getElementById('info-filename').textContent = filename || '-';
    document.getElementById('info-type').textContent = type ? type.toUpperCase() : '-';
    
    // Set current date as "Added" date
    const today = new Date();
    const dateStr = today.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
    document.getElementById('info-date').textContent = dateStr;
}

// Handle keyboard navigation
function handleKeyboard(event) {
    const modal = document.getElementById('previewModal');
    if (modal.style.display !== 'block') return;
    
    switch(event.key) {
        case 'Escape':
            closeModal();
            break;
        case 'ArrowLeft':
            navigateMedia('prev');
            break;
        case 'ArrowRight':
            navigateMedia('next');
            break;
    }
}

// Close modal when clicking outside of it
window.onclick = function(event) {
    const modal = document.getElementById('previewModal');
    if (event.target === modal) {
        closeModal();
    }
}
