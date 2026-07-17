import React, {useState} from 'react';

const Pagination = ({
                        currentPage,
                        totalPages,
                        onPageChange,
                        itemsPerPage,
                        totalItems,
                        // OpenSearch max_result_window: from + size must be <= 10000
                        maxItems = 10000
                    }) => {

    const selections = [...new Set([10, 25, 50, 100, itemsPerPage])].sort((a, b) => a - b);

    const [editingPage, setEditingPage] = useState(false);
    const [pageInput, setPageInput] = useState('');

    const maxReachablePage = Math.max(1, Math.floor(maxItems / itemsPerPage));
    const effectiveTotalPages = Math.min(totalPages, maxReachablePage);
    const isCapped = totalPages > maxReachablePage;

    const handleFirst = () => {
        onPageChange(1, itemsPerPage);
    }
    const handlePrevious = () => {
        if (currentPage > 1) {
            onPageChange(currentPage - 1, itemsPerPage);
        }
    };

    const handleNext = () => {
        if (currentPage < effectiveTotalPages) {
            onPageChange(currentPage + 1, itemsPerPage);
        }
    };

    const handleLast = () => {
        onPageChange(effectiveTotalPages, itemsPerPage);
    };

    const handlePage = (page) => {
        if (page !== currentPage) {
            onPageChange(page, itemsPerPage);
        }
    };

    const getPageNumbers = () => {
        // Fenêtre glissante : 1 … 4 5 [6] 7 8 … 20
        if (effectiveTotalPages <= 7) {
            return Array.from({length: effectiveTotalPages}, (_, i) => i + 1);
        }
        const pages = [1];
        const start = Math.max(2, currentPage - 1);
        const end = Math.min(effectiveTotalPages - 1, currentPage + 1);
        if (start > 2) {
            pages.push('ellipsis-left');
        }
        for (let i = start; i <= end; i++) {
            pages.push(i);
        }
        if (end < effectiveTotalPages - 1) {
            pages.push('ellipsis-right');
        }
        pages.push(effectiveTotalPages);
        return pages;
    };

    const startPageEdit = () => {
        setPageInput(String(currentPage));
        setEditingPage(true);
    };

    const commitPageEdit = () => {
        setEditingPage(false);
        const page = parseInt(pageInput, 10);
        if (!isNaN(page)) {
            handlePage(Math.min(Math.max(1, page), effectiveTotalPages));
        }
    };

    const handlePageInputKeyDown = (e) => {
        if (e.key === 'Enter') {
            commitPageEdit();
        } else if (e.key === 'Escape') {
            setEditingPage(false);
        }
    };

    const handleItemsPerPageInput = (e) => {
        const value = e.target.value;
        if (value === '' || (/^\d+$/.test(value) && Number(value) > 0)) {
            const newSize = Number(value);
            // Re-clamp the current page so from + size never exceeds maxItems
            const newMaxPage = Math.max(1, Math.floor(maxItems / newSize));
            onPageChange(Math.min(currentPage, newMaxPage), newSize);
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
            {isCapped && (
                <span
                    className="text-xs text-gray-500 min-w-max"
                    title={`Seuls les ${maxItems.toLocaleString('fr-FR')} premiers résultats sont accessibles. Affinez la recherche pour voir les suivants.`}
                >
                    (limité aux {maxItems.toLocaleString('fr-FR')} premiers)
                </span>
            )}
            <button
                onClick={handleFirst}
                disabled={currentPage === 1}
                className="p-1 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                aria-label="First page"
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
            {getPageNumbers().map(page => (
                typeof page === 'number' ? (
                    page === currentPage && editingPage ? (
                        <input
                            key={page}
                            type="text"
                            inputMode="numeric"
                            value={pageInput}
                            autoFocus
                            onFocus={(e) => e.target.select()}
                            onChange={(e) => {
                                if (e.target.value === '' || /^\d+$/.test(e.target.value)) {
                                    setPageInput(e.target.value);
                                }
                            }}
                            onKeyDown={handlePageInputKeyDown}
                            onBlur={commitPageEdit}
                            aria-label="Aller à la page"
                            className="w-14 px-1 py-1 rounded border border-blue-500 text-sm text-center focus:ring-2 focus:ring-blue-200 focus:outline-none"
                        />
                    ) : (
                        <button
                            key={page}
                            onClick={() => handlePage(page)}
                            onDoubleClick={page === currentPage ? startPageEdit : undefined}
                            title={page === currentPage ? 'Double-cliquez pour saisir un numéro de page' : undefined}
                            aria-label={`Page ${page}`}
                            aria-current={page === currentPage ? 'page' : undefined}
                            className={`min-w-[2rem] px-2 py-1 rounded text-sm ${
                                page === currentPage
                                    ? 'bg-blue-600 text-white font-semibold cursor-text'
                                    : 'text-gray-700 hover:bg-gray-100'
                            }`}
                        >
                            {page}
                        </button>
                    )
                ) : (
                    <button
                        key={page}
                        onClick={startPageEdit}
                        title="Aller à une page précise"
                        aria-label="Aller à une page précise"
                        className="px-1 text-sm text-gray-500 rounded hover:bg-gray-100 hover:text-gray-700"
                    >
                        …
                    </button>
                )
            ))}
            <button
                onClick={handleNext}
                disabled={currentPage >= effectiveTotalPages || totalItems === 0}
                className="p-1 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                aria-label="Next page"
            >
                <svg className="h-5 w-5 text-gray-700" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" /></svg>
            </button>
            <button
                onClick={handleLast}
                disabled={currentPage >= effectiveTotalPages || totalItems === 0}
                className="p-1 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                aria-label="Last page"
            >
                <svg className="h-5 w-5 text-gray-700" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 5l7 7-7 7" />
                </svg>
            </button>
        </div>
    );
};

export default Pagination
