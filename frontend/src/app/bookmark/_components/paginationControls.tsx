
import { Button } from '@/components/ui/button';

interface PaginationControlsProps {
    currentPage: number;
    totalPages: number;
    onPrevious: () => void;
    onNext: () => void;
}
export function PaginationControls({ currentPage, totalPages, onPrevious, onNext }: PaginationControlsProps) {
    if (totalPages <= 1) return null;
    return (
        <div className="flex justify-center items-center mt-8 space-x-4">
            <Button
                onClick={onPrevious}
                disabled={currentPage === 0}
                variant="outline"
            >
                이전
            </Button>
            <span className="test-sm">
                {currentPage + 1} / {totalPages}
            </span>
            <Button
                onClick={onNext}
                disabled={currentPage +1 >= totalPages}
                variant="outline"
            >
                다음
            </Button>
        </div>
    );
}