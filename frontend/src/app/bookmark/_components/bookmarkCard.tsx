import { Bookmark } from "@/types/bookmarkData";

interface BookmarkCardProps {
    bookmark: Bookmark;
}
export function BookmarkCard({ bookmark } : BookmarkCardProps ) {
    return (
        <Card key={bookmark.id} className="h-full cursor-pointer hover:shadow-lg transition-shadow overflow-hidden" onClick={() => onNavigate(`/bookmark/${bookmark.id}`)}>
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <CardTitle className="line-clamp-2">{bookmark.book.title}</CardTitle>
              <CardDescription>{bookmark.book?.authors?.join(', ') || '저자 정보 없음'}</CardDescription>
              <Badge className={`mt-2 ${getReadStateColor(bookmark.readState)}`}>
                {getReadState(bookmark.readState)}
              </Badge>
            </div>
            <ImageWithFallback
              src={bookmark.book.imageUrl}
              alt={bookmark.book.title}
              className="w-16 h-24 object-cover rounded ml-4"
            />
          </div>
        </CardHeader>
        <CardContent className="flex-grow">
          <div className="space-y-4">
            <div className="flex justify-between text-sm text-muted-foreground">
              <span>카테고리: {bookmark.book.category}</span>
              <span>{bookmark.book.totalPage} 쪽</span>
            </div>
            {bookmark.readState === 'READING' && bookmark.readPage && (
              <div className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span>{bookmark.readPage}쪽 / {bookmark.book.totalPage}쪽</span>
                  <span>{bookmark.readingRate}%</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${bookmark.readingRate}%` }}></div>
                </div>
              </div>
            )}
          </div>
          {/* 카드 평점 */}
          {bookmark.review?.rate > 0 && (
            <p className="flex items-center space-x-1 py-2">
              {renderStars(bookmark.review.rate)}
              <span className="text-sm ml-2">{bookmark.review.rate}</span>
            </p>
          )}
          {bookmark.review?.content && (
            <p className="text-sm text-muted-foreground line-clamp-2 gap-2 py-2">
              {bookmark.review.content}
            </p>
          )}
          {/* 날짜 정보 */}
          <div className="text-xs text-muted-foreground gap-2 py-2">
            {bookmark.readState === 'READ' ? `완독 : ${bookmark.endReadDate?.substring(0, 10)}` : bookmark.readState === 'READING' ? `시작 : ${bookmark.startReadDate?.substring(0, 10)}` : `추가 : ${bookmark.createDate?.substring(0, 10)}`}
          </div>
        </CardContent>
        <CardFooter className="flex justify-end space-x-2 py-3">
          {/* 북마크 편집 버튼 */}
            <div className="flex space-x-2" onClick={(e) => e.stopPropagation()}>
              <Dialog open={isEditDialogOpen && editBookmark?.id === bookmark.id} onOpenChange={setIsEditDialogOpen}>
                <DialogTrigger asChild>
                  <Button variant="ghost" size="sm" onClick={(e) => {
                    e.stopPropagation();
                    setEditBookmark(bookmark);
                    setIsEditDialogOpen(true);
                  }}
                  >
                    <Edit className="h-4 w-4" />
                  </Button>
                </DialogTrigger>
                <DialogContent className="max-w-2xl">
                  <DialogHeader>
                    <DialogTitle>북마크 편집</DialogTitle>
                    <DialogDescription>{bookmark.book.title}의 정보를 수정하세요.</DialogDescription>
                  </DialogHeader>
                  {editBookmark && (
                    <BookmarkEditForm
                      bookmark={editBookmark} onSave={handleSaveBookmark} onCancel={() => {
                        setEditBookmark(null);
                        setIsEditDialogOpen(false);
                      }}
                    />
                  )}
                </DialogContent>
              </Dialog>
              <Button
                variant="ghost"
                size="sm"
                onClick={(e) => {
                  e.stopPropagation();
                  handleDeleteBookmark(bookmark.id);
                }}
              >
                <Trash2 className='h-4 w-4' />
              </Button>
            </div>
        </CardFooter>
      </Card>
    );
}