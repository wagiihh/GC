
function openExportModal() {
    document.getElementById('exportModal').style.display = 'flex';
}

function closeExportModal() {
    document.getElementById('exportModal').style.display = 'none';
}

      const exportBtn = document.querySelector('#exportBtn');
    const fileTypeModal = document.querySelector('#fileTypeModal');
    const closeModalBtn = document.querySelector('#closeModal');

    exportBtn.addEventListener('click', function() {
        fileTypeModal.style.display = 'flex';
    });

    closeModalBtn.addEventListener('click', function() {
        fileTypeModal.style.display = 'none';
    });

    window.onclick = function(event) {
        if (event.target === fileTypeModal) {
            fileTypeModal.style.display = 'none';
        }
    };

    function filterAssets() {
        const searchTerm = document.getElementById('searchBar').value.toLowerCase();
        const selectedType = document.getElementById('assetTypeFilter').value.toLowerCase();
        const rows = document.querySelectorAll('#assetTableBody tr');

        rows.forEach(function (row) {
            const assetType = row.querySelector('.assetTypeCell').textContent.toLowerCase();
            const assetID = row.cells[1].textContent.toLowerCase();
            const assetName = row.cells[2].textContent.toLowerCase();
            const assetSerial = row.cells[3].textContent.toLowerCase();
            const assetModel = row.cells[4].textContent.toLowerCase();

            // Check if the row matches the search term
            const matchesSearch = assetType.includes(searchTerm) ||
                assetID.includes(searchTerm) ||
                assetName.includes(searchTerm) ||
                assetSerial.includes(searchTerm) ||
                assetModel.includes(searchTerm);

            // Check if the row matches the selected asset type
            const matchesType = (selectedType === 'all') || (assetType === selectedType);

            // Display the row only if it matches both search and type filters
            if (matchesSearch && matchesType) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    }

    function exportToCSV() {
        const rows = document.querySelectorAll('#assetTableBody tr');
        let csv = [];

        // Extract the headers
        let headers = Array.from(document.querySelectorAll('thead th:not(:last-child)'))
            .map(header => header.innerText);
        csv.push(headers.join(','));

        // Extract visible rows
        rows.forEach(function (row) {
            if (row.style.display !== 'none') {
                let cols = Array.from(row.querySelectorAll('td:not(:last-child)'))
                    .map(col => col.innerText);
                csv.push(cols.join(','));
            }
        });

        let csvFile = new Blob([csv.join('\n')], { type: 'text/csv' });
        saveAs(csvFile, 'assets.csv');
    }

    function exportToExcel() {
        let html = '<table border="1" cellspacing="0" cellpadding="5"><thead><tr>';

        // Get the headers
        let headers = Array.from(document.querySelectorAll('thead th:not(:last-child)'))
            .map(header => `<th>${header.innerText}</th>`).join('');
        html += headers + '</tr></thead><tbody>';

        // Get the rows
        document.querySelectorAll('#assetTableBody tr').forEach(row => {
            if (row.style.display !== 'none') {
                let cells = Array.from(row.querySelectorAll('td:not(:last-child)'))
                    .map(cell => `<td>${cell.innerText}</td>`).join('');
                html += `<tr>${cells}</tr>`;
            }
        });

        html += '</tbody></table>';

        let excelFile = new Blob([html], { type: 'application/vnd.ms-excel' });
        saveAs(excelFile, 'assets.xls');
    }

    function exportToPDF() {
const { jsPDF } = window.jspdf;
const doc = new jsPDF();

// Load the logo image
const logo = new Image();
logo.src = '../images/canalsugar.png'; // Replace with the correct path to your logo

logo.onload = function () {
    const imgWidth = 50; // Adjust the image width
    const imgHeight = 30; // Adjust the image height
    const pageWidth = doc.internal.pageSize.getWidth();
    const xOffset = (pageWidth - imgWidth) / 2; // Center the image

    // Add the logo to the PDF
    doc.addImage(logo, 'PNG', xOffset, 10, imgWidth, imgHeight); // Position and size of the logo

    // Add the title below the logo
    doc.setFontSize(18);
    doc.setTextColor(22, 93, 10); // Dark green color
    doc.text('CANAL SUGAR ASSET REPORT', pageWidth / 2, imgHeight + 25, { align: 'center' }); // Center the title

    // Add some space between the title and the table
    doc.setFontSize(12);
    doc.text('', 14, imgHeight + 35);

    // Initialize the table data
    let data = [];
    let headers = [];

    // Extract the headers
    headers = Array.from(document.querySelectorAll('thead th:not(:last-child)'))
        .map(header => header.innerText);
    data.push(headers);

    // Extract visible rows
    const rows = document.querySelectorAll('#assetTableBody tr');
    rows.forEach(function (row) {
        if (row.style.display !== 'none') {
            let cols = Array.from(row.querySelectorAll('td:not(:last-child)'))
                .map(col => col.innerText);
            data.push(cols);
        }
    });

    // Use autoTable to add the table to the PDF
    doc.autoTable({
        startY: imgHeight + 40, // Start the table after the title and logo
        head: [data[0]],
        body: data.slice(1),
        headStyles: {
            fillColor: [22, 93, 10] // Dark green color for header cells
        },
        styles: {
            fontSize: 10, // Adjust font size
        }
    });

    // Save the PDF
    doc.save('assets_report.pdf');
};
}



    const pdf_btn = document.querySelector('#toPDF');
    pdf_btn.onclick = () => {
        exportToPDF();
    };

    document.addEventListener('DOMContentLoaded', function() {
const deleteLinks = document.querySelectorAll('#delete');
const modal = document.getElementById('deleteModal');
const confirmDeleteBtn = document.getElementById('confirmDelete');
const cancelDeleteBtn = document.getElementById('cancelDelete');
let deleteUrl = '';

deleteLinks.forEach(function(link) {
    link.addEventListener('click', function(event) {
        event.preventDefault();
        deleteUrl = this.href; // Store the delete URL
        modal.style.display = 'flex'; // Show the modal
    });
});

confirmDeleteBtn.addEventListener('click', function() {
    window.location.href = deleteUrl; // Navigate to the delete URL
});

cancelDeleteBtn.addEventListener('click', function() {
    modal.style.display = 'none'; 
});

window.onclick = function(event) {
    if (event.target === modal) {
        modal.style.display = 'none';
    }
};
});