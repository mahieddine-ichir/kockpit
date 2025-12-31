function TruncateWithTooltip({ text, maxLength }) {
    if (!text) return null;
    const shouldTruncate = maxLength && text.length > maxLength;
    return (
        <span title={text} style={{ cursor: shouldTruncate ? 'pointer' : 'default' }}>
        {shouldTruncate ? text.slice(0, maxLength) + '…' : text}
      </span>
    );
}

export default TruncateWithTooltip;