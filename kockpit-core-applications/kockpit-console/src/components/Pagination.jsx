import React from 'react';

const Pagination = ({
                        currentPage,
                        totalPages,
                        onPageChange,
                        itemsPerPage,
                        totalItems
                    }) => {

    const selections = [...new Set([10, 25, 50, 100, itemsPerPage])].sort((a, b) => a - b);
    const handleFirst = () => {
        onPageChange(1, itemsPerPage);
    }
    const handlePrevious = () => {
        if (currentPage > 1) {
            onPageChange(currentPage - 1, itemsPerPage);
        }
    };

    const handleNext = () => {
        if (currentPage < totalPages) {
            onPageChange(currentPage + 1, itemsPerPage);
        }
    };

    const handleItemsPerPageInput = (e) => {
        const value = e.target.value;
        if (value === '' || (/^\d+$/.test(value) && Number(value) > 0)) {
            onPageChange(currentPage, Number(value));
        }
    };

    const startItem = totalItems === 0 ? 0 : (currentPage - 1) * itemsPerPage + 1;
    const endItem = Math.min((currentPage) * itemsPerPage, totalItems);

    return (
        <div className="flex items-center justify-end space-x-2 w-full mb-1">
            <div className="flex items-center space-x-2">
                <select
                    value={itemsPerPage}
                    onChange={handleItemsPerPageInput}
                    className="rounded border border-gray-300 px-2 py-1 text-sm bg-white focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:outline-none w-20"
                >
                    {
                        selections.map(value => {
                            return (
                                <option key={value}>{value}</option>
                            )
                        })
                    }
                </select>
            </div>
            <span className="text-sm text-gray-700 min-w-max">{startItem} - {endItem} of {' '}
                <span className="font-semibold text-blue-700">{totalItems}</span>
            </span>
            <button
                onClick={handleFirst}
                disabled={currentPage === 1}
                className="p-1 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                aria-label="Previous page"
            >
                <svg className="h-5 w-5 text-gray-700" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19 19l-7-7 7-7" />
                </svg>
            </button>
            <button
                onClick={handlePrevious}
                disabled={currentPage === 1}
                className="p-1 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                aria-label="Previous page"
            >
                <svg className="h-5 w-5 text-gray-700" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" /></svg>
            </button>
            <button
                onClick={handleNext}
                disabled={currentPage === totalPages || totalItems === 0}
                className="p-1 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                aria-label="Next page"
            >
                <svg className="h-5 w-5 text-gray-700" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" /></svg>
            </button>
        </div>
    );
};

export default Pagination