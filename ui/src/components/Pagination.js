import React, { useState } from "react";

function Pagination({ totalItems, pageSize, onPageChange }) {
    const totalPages = Math.ceil(totalItems / pageSize);
    const [currentPage, setCurrentPage] = useState(1); // Self-managed state for current page

    const handlePageClick = (page) => {
        setCurrentPage(page); // Update internal state for page tracking
        if (onPageChange) {
            onPageChange(page); // Notify parent of page change, if needed
        }
    };

    return (
        <div className="pagination">
            <button
                disabled={currentPage === 1}
                onClick={() => handlePageClick(currentPage - 1)}
            >
                Previous
            </button>
            {Array.from({ length: totalPages }, (_, index) => (
                <button
                    key={index + 1}
                    className={index + 1 === currentPage ? "active" : ""}
                    onClick={() => handlePageClick(index + 1)}
                >
                    {index + 1}
                </button>
            ))}
            <button
                disabled={currentPage === totalPages}
                onClick={() => handlePageClick(currentPage + 1)}
            >
                Next
            </button>
        </div>
    );
}

export default Pagination;
