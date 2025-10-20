function openExportModal() {
    document.getElementById('exportModal').style.display = 'flex';
}

function closeExportModal() {
    document.getElementById('exportModal').style.display = 'none';
}


    //    // Export to CSV Function
    //    function openModal() {
    //         document.getElementById('exportModal').style.display = 'block';
    //     }

    //     function closeModal() {
    //         document.getElementById('exportModal').style.display = 'none';
    //     }

    //     // Close the modal if the user clicks outside of it
    //     window.onclick = function(event) {
    //         if (event.target === document.getElementById('exportModal')) {
    //             closeModal();
    //         }
    //     };

        // Export functions
        function exportToCSV() {
            const rows = document.querySelectorAll('#hiddenAssetTable tbody tr');
            let csv = [];
            
            // Extract the headers
            let headers = Array.from(document.querySelectorAll('#hiddenAssetTable thead th'))
                .map(header => header.innerText);
            csv.push(headers.join(','));

            // Extract rows from hidden table
            rows.forEach(function (row) {
                let cols = Array.from(row.querySelectorAll('td'))
                    .map(col => col.innerText);
                csv.push(cols.join(','));
            });

            let csvFile = new Blob([csv.join('\n')], { type: 'text/csv' });
            saveAs(csvFile, 'stock_report.csv');
        }

        function exportToExcel() {
            let html = '<table border="1" cellspacing="0" cellpadding="5"><thead><tr>';
            
            // Get the headers
            let headers = Array.from(document.querySelectorAll('#hiddenAssetTable thead th'))
                .map(header => `<th>${header.innerText}</th>`).join('');
            html += headers + '</tr></thead><tbody>';
            
            // Get the rows from hidden table
            document.querySelectorAll('#hiddenAssetTable tbody tr').forEach(row => {
                let cells = Array.from(row.querySelectorAll('td'))
                    .map(cell => `<td>${cell.innerText}</td>`).join('');
                html += `<tr>${cells}</tr>`;
            });

            html += '</tbody></table>';

            let excelFile = new Blob([html], { type: 'application/vnd.ms-excel' });
            saveAs(excelFile, 'stock_report.xls');
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
                doc.text('CANAL SUGAR STOCK REPORT', pageWidth / 2, 50, { align: 'center' }); // Adjusted y position for the title

                // Add some space between the title and the table
                doc.setFontSize(12);
                doc.text('', 14, 55); // Added vertical space after the title

                let data = [];
                let headers = [];

                // Extract the headers
                headers = Array.from(document.querySelectorAll('#hiddenAssetTable thead th'))
                    .map(header => header.innerText);
                data.push(headers);

                // Extract rows from hidden table
                const rows = document.querySelectorAll('#hiddenAssetTable tbody tr');
                rows.forEach(function (row) {
                    let cols = Array.from(row.querySelectorAll('td'))
                        .map(col => col.innerText);
                    data.push(cols);
                });

                // Use autoTable to add the table to the PDF
                doc.autoTable({
                    startY: 65, // Start the table after the title and spacing
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
                doc.save('stock_report.pdf');
            };
        }

        // Attach PDF export functionality to button
        const pdf_btn = document.querySelector('#toPDF');
        pdf_btn.onclick = () => {
            exportToPDF();
        };