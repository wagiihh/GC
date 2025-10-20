const imageSlider = document.getElementById('imageSlider');
const images = [
    '../images/Factory.jpg',
    '../images/testing.png',
    '../images/white.jpg',
    '../images/Sugar-factory.jpg'
];

let currentIndex = 0;

function changeImage() {
    imageSlider.style.backgroundImage = `url(${images[currentIndex]})`;
    currentIndex = (currentIndex + 1) % images.length;
}

// Initial image setup
changeImage();

// Change image every 5 seconds (5000 milliseconds)
setInterval(changeImage, 5000);