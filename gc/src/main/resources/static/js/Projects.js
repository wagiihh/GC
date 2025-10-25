// Projects Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Initialize the page
    initProjectsPage();
});

function initProjectsPage() {
    // Add smooth scrolling for internal links
    addSmoothScrolling();
    
    // Add hover effects for images
    addImageHoverEffects();
    
    // Add scroll animations
    addScrollAnimations();
    
    // Add click handlers for film items
    addFilmClickHandlers();
}

function addSmoothScrolling() {
    const links = document.querySelectorAll('a[href^="#"]');
    
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

function addImageHoverEffects() {
    const images = document.querySelectorAll('.trilogy-image, .book-cover, .main-image');
    
    images.forEach(image => {
        image.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.02)';
            this.style.transition = 'transform 0.3s ease';
        });
        
        image.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    });
}

function addScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // Observe elements for animation
    const animatedElements = document.querySelectorAll('.film-item, .main-title, .book-cover-container, .image-section');
    
    animatedElements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(element);
    });
}

function addFilmClickHandlers() {
    const filmItems = document.querySelectorAll('.film-item');
    
    filmItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Add click animation
            this.style.transform = 'scale(0.98)';
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 150);
            
            // Navigate to the project page
            const href = this.getAttribute('href');
            if (href) {
                setTimeout(() => {
                    window.location.href = href;
                }, 200);
            }
        });
    });
}

// Add scroll indicator functionality
function addScrollIndicator() {
    const scrollIndicator = document.querySelector('.scroll-indicator');
    
    if (scrollIndicator) {
        scrollIndicator.addEventListener('click', function() {
            const nextSection = document.querySelector('.x-trilogy-section');
            if (nextSection) {
                nextSection.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    }
}

// Add parallax effect for hero section
function addParallaxEffect() {
    const heroSection = document.querySelector('.hero-section');
    const heroBackground = document.querySelector('.hero-background');
    
    if (heroSection && heroBackground) {
        window.addEventListener('scroll', function() {
            const scrolled = window.pageYOffset;
            const rate = scrolled * -0.5;
            
            heroBackground.style.transform = `translateY(${rate}px)`;
        });
    }
}

// Add menu toggle functionality
function addMenuToggle() {
    const menuToggle = document.querySelector('.menu-toggle');
    
    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            // Toggle menu animation
            const spans = this.querySelectorAll('span');
            spans.forEach((span, index) => {
                if (index === 0) {
                    span.style.transform = 'rotate(45deg) translate(5px, 5px)';
                } else if (index === 1) {
                    span.style.opacity = '0';
                } else if (index === 2) {
                    span.style.transform = 'rotate(-45deg) translate(7px, -6px)';
                }
            });
            
            // Add menu functionality here
            console.log('Menu toggled');
        });
    }
}

// Add search functionality
function addSearchFunctionality() {
    const searchIcon = document.querySelector('.search-icon');
    
    if (searchIcon) {
        searchIcon.addEventListener('click', function() {
            // Add search functionality here
            console.log('Search clicked');
        });
    }
}

// Initialize additional features
document.addEventListener('DOMContentLoaded', function() {
    addScrollIndicator();
    addParallaxEffect();
    addMenuToggle();
    addSearchFunctionality();
});

// Add keyboard navigation
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        // Close any open menus or modals
        const menuToggle = document.querySelector('.menu-toggle');
        if (menuToggle) {
            const spans = menuToggle.querySelectorAll('span');
            spans.forEach((span, index) => {
                span.style.transform = 'none';
                span.style.opacity = '1';
            });
        }
    }
});

// Add loading animation
function addLoadingAnimation() {
    const body = document.body;
    body.style.opacity = '0';
    
    window.addEventListener('load', function() {
        body.style.transition = 'opacity 0.5s ease';
        body.style.opacity = '1';
    });
}

// Initialize loading animation
addLoadingAnimation();
