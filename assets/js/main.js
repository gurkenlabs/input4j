// Copy button functionality
function copyCode(button) {
  const codeBlock = button.closest('.code-block').querySelector('pre code, pre');
  if (codeBlock) {
    navigator.clipboard.writeText(codeBlock.textContent).then(() => {
      const original = button.textContent;
      button.textContent = 'Copied!';
      setTimeout(() => button.textContent = original, 2000);
    });
  }
}

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
  anchor.addEventListener('click', function(e) {
    const target = document.querySelector(this.getAttribute('href'));
    if (target) {
      e.preventDefault();
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });
});
