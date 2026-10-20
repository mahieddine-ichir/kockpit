// "Failed" must read without telling red apart (colour-blind readers): every view marks it with
// the same three cues - a dashed dark border, an X icon and a hatched fill - on top of the red.
export const FAILED_BORDER = 'border-dashed border-red-700';

// Diagonal hatching: light stripes on a solid fill (bars, segments), dark ones on a pale fill (tiles)
export const hatch = (onSolid) => ({
    backgroundImage: `repeating-linear-gradient(135deg, ${onSolid ? 'rgba(255,255,255,0.55)' : 'rgba(127,29,29,0.18)'} 0 2px, transparent 2px 6px)`,
});
