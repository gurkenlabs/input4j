// input4j Website JavaScript

document.addEventListener('DOMContentLoaded', function() {
  // Initialize all interactive elements
  initializeNavigation();
  initializeAnimations();
  initializeCopyButtons();
  initializeScrollEffects();
});

// Navigation functionality
function initializeNavigation() {
  const hamburger = document.querySelector('.hamburger');
  const mobileNav = document.getElementById('mobileNav');
  
  if (hamburger && mobileNav) {
    hamburger.addEventListener('click', function() {
      mobileNav.style.display = mobileNav.style.display === 'flex' ? 'none' : 'flex';
      hamburger.classList.toggle('active');
    });
  }
  
  // Close mobile nav when clicking on a link
  const mobileLinks = mobileNav?.querySelectorAll('.mobile-link');
  if (mobileLinks) {
    mobileLinks.forEach(link => {
      link.addEventListener('click', function() {
        mobileNav.style.display = 'none';
        hamburger.classList.remove('active');
      });
    });
  }
}

// Copy button functionality
function initializeCopyButtons() {
  const copyButtons = document.querySelectorAll('.copy-btn');
  
  copyButtons.forEach(button => {
    button.addEventListener('click', function() {
      const codeBlock = this.closest('.code-snippet').querySelector('pre code, pre');
      if (codeBlock) {
        copyToClipboard(codeBlock.textContent);
        showCopySuccess(this);
      }
    });
  });
}

function copyToClipboard(text) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text);
  } else {
    // Fallback for older browsers
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
  }
}

function showCopySuccess(button) {
  const originalText = button.textContent;
  button.textContent = '✓ Copied';
  button.style.backgroundColor = 'var(--color-primary-dark)';
  
  setTimeout(() => {
    button.textContent = originalText;
    button.style.backgroundColor = '';
  }, 2000);
}

// Smooth scroll for navigation links
function initializeScrollEffects() {
  const navLinks = document.querySelectorAll('.nav-link[href^="#"], .mobile-link[href^="#"]');
  
  navLinks.forEach(link => {
    link.addEventListener('click', function(e) {
      const targetId = this.getAttribute('href').substring(1);
      const targetElement = document.getElementById(targetId);
      
      if (targetElement) {
        e.preventDefault();
        
        const headerOffset = 80;
        const elementPosition = targetElement.getBoundingClientRect().top;
        const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
        
        window.scrollTo({
          top: offsetPosition,
          behavior: 'smooth'
        });
      }
    });
  });
}

// Animation utilities
function initializeAnimations() {
  // Add intersection observer for fade-in animations
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };
  
  const observer = new IntersectionObserver(function(entries) {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in');
        observer.unobserve(entry.target);
      }
    });
  }, observerOptions);
  
  // Observe feature cards and stat cards
  const animatedElements = document.querySelectorAll('.feature-card, .stat-card');
  animatedElements.forEach(el => {
    observer.observe(el);
  });
}

// Utility functions
function toggleMobileNav() {
  const mobileNav = document.getElementById('mobileNav');
  const hamburger = document.querySelector('.hamburger');
  
  if (mobileNav && hamburger) {
    const isDisplayed = mobileNav.style.display === 'flex';
    mobileNav.style.display = isDisplayed ? 'none' : 'flex';
    hamburger.classList.toggle('active');
  }
}

// Add scroll effect to navigation
window.addEventListener('scroll', function() {
  const nav = document.querySelector('.glassy-nav');
  const scrollPosition = window.scrollY;
  
  if (nav && scrollPosition > 100) {
    nav.style.background = 'rgba(72, 143, 156, 0.8)';
    nav.style.backdropFilter = 'blur(15px)';
  } else if (nav) {
    nav.style.background = 'var(--mica-active)';
    nav.style.backdropFilter = 'blur(20px)';
  }
});

// Add hover effects to interactive elements
document.addEventListener('DOMContentLoaded', function() {
  // Add ripple effect to buttons
  const buttons = document.querySelectorAll('.btn');
  
  buttons.forEach(button => {
    button.addEventListener('click', function(e) {
      const ripple = document.createElement('span');
      const rect = this.getBoundingClientRect();
      const size = Math.max(rect.width, rect.height);
      const x = e.clientX - rect.left - size / 2;
      const y = e.clientY - rect.top - size / 2;
      
      ripple.style.width = ripple.style.height = size + 'px';
      ripple.style.left = x + 'px';
      ripple.style.top = y + 'px';
      ripple.classList.add('ripple');
      
      this.appendChild(ripple);
      
      setTimeout(() => {
        ripple.remove();
      }, 600);
    });
  });
});

// Add CSS for ripple effect
const style = document.createElement('style');
style.textContent = `
  .ripple {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.6);
    transform: scale(0);
    animation: ripple-animation 0.6s ease-out;
    pointer-events: none;
  }
  
  @keyframes ripple-animation {
    to {
      transform: scale(4);
      opacity: 0;
    }
  }
  
  .hamburger.active .hamburger-line:nth-child(1) {
    transform: rotate(-45deg) translate(-5px, 6px);
  }
  
  .hamburger.active .hamburger-line:nth-child(2) {
    opacity: 0;
  }
  
  .hamburger.active .hamburger-line:nth-child(3) {
    transform: rotate(45deg) translate(-5px, -6px);
  }
`;
document.head.appendChild(style);

// Add animate-in class for intersection observer
const animateStyle = document.createElement('style');
animateStyle.textContent = `
  .animate-in {
    animation: fadeInUp 0.6s ease-out forwards;
  }
  
  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
`;
document.head.appendChild(animateStyle);