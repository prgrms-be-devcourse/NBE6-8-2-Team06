"use client"
import withLogin from "@/app/_hooks/withLogin";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ArrowLeft, Edit, FileText, Plus, Save, Search, Trash2, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { use, useState } from "react";


interface BookNotesPageProps {
    bookId: number | null;
    onNavigate: (page: string) => void;
  }
  
  interface MyBook {
    id: number;
    title: string;
    author: string;
    category: string;
    totalPages: number;
  }
  
  interface Note {
    id: number;
    bookId: number;
    title: string;
    content: string;
    page?: number;
    createdDate: string;
    updatedDate?: string;
  }

  export default withLogin(function page({params}:{params:Promise<{id:string}>}){

    const {id:bookIdStr} = use(params);
    const bookId = parseInt(bookIdStr);
      
    const [searchTerm, setSearchTerm] = useState('');
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [editingNote, setEditingNote] = useState<Note | null>(null);
    const [noteForm, setNoteForm] = useState({
      title: '',
      content: '',
      page: ''
    });


  const router = useRouter();
  const onNavigate = (e:string) => {
    router.push(e);
  }

  // 내 책 데이터
  const myBooks: MyBook[] = [
    {
      id: 1,
      title: "클린 코드",
      author: "로버트 C. 마틴",
      category: "프로그래밍",
      totalPages: 464
    }
  ];

  // 노트 데이터
  const [notes, setNotes] = useState<Note[]>([
    {
      id: 1,
      bookId: 1,
      title: "의미 있는 이름",
      content: "변수명, 함수명, 클래스명은 존재 이유, 수행 기능, 사용 방법이 드러나야 한다. 주석이 필요하다면 의미를 명확히 드러내지 못했다는 말이다.\n\n좋은 이름을 선택하려면 시간이 걸리지만 좋은 이름으로 절약하는 시간이 훨씬 더 많다.",
      page: 22,
      createdDate: "2024-01-20",
      updatedDate: "2024-01-20"
    },
    {
      id: 2,
      bookId: 1,
      title: "함수 작성 규칙",
      content: "함수는 작게 만들어야 한다. 함수가 하는 일은 하나여야 한다. 함수 당 추상화 수준은 하나로 제한한다.\n\n함수는 이야기를 들려준다. 프로그래밍 언어는 그 이야기를 풀어가는 수단이다.",
      page: 42,
      createdDate: "2024-01-25",
      updatedDate: "2024-01-26"
    },
    {
      id: 3,
      bookId: 1,
      title: "주석 사용법",
      content: "나쁜 코드에 주석을 달지 마라. 새로 짜라. 코드로 의도를 표현하지 못해 실패를 만회하기 위해 주석을 사용한다.\n\n진실은 한곳에만 존재한다. 바로 코드다. 코드만이 자기가 하는 일을 진실되게 말한다.",
      page: 68,
      createdDate: "2024-02-02",
      updatedDate: "2024-02-02"
    },
    {
      id: 4,
      bookId: 1,
      title: "오류 처리",
      content: "오류 처리는 중요하다. 하지만 오류 처리 코드로 인해 프로그램 논리를 이해하기 어려워진다면 깨끗한 코드라 부르기 어렵다.\n\n예외를 던질 때는 전후 상황을 충분히 덧붙인다. 그러면 오류가 발생한 원인과 위치를 찾기가 쉬워진다.",
      page: 130,
      createdDate: "2024-02-08"
    },
    {
      id: 5,
      bookId: 1,
      title: "클래스 설계",
      content: "클래스는 작아야 한다. 함수와 마찬가지로 클래스를 만들 때도 크기가 첫 번째 규칙이다.\n\n클래스 이름은 해당 클래스의 책임을 기술해야 한다. 실제로 클래스 이름은 클래스 책임을 기술하는 가장 좋은 방법이다.",
      createdDate: "2024-02-12"
    }
  ]);

  const book = myBooks.find(b => b.id === bookId);
  const bookNotes = notes.filter(note => note.bookId === bookId);

  const filteredNotes = bookNotes.filter(note => 
    note.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    note.content.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (!book) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <p>책을 찾을 수 없습니다.</p>
          <Button onClick={() => onNavigate('/bookmark')} className="mt-4">
            내 책 목록으로 돌아가기
          </Button>
        </div>
      </div>
    );
  }

  const handleSaveNote = () => {
    if (!noteForm.title.trim() || !noteForm.content.trim()) return;

    const now = new Date().toISOString().split('T')[0];
    
    if (editingNote) {
      // 수정
      setNotes(prev => prev.map(note => 
        note.id === editingNote.id 
          ? {
              ...note,
              title: noteForm.title,
              content: noteForm.content,
              page: noteForm.page ? parseInt(noteForm.page) : undefined,
              updatedDate: now
            }
          : note
      ));
    } else {
      // 새 노트 추가
      const newNote: Note = {
        id: Date.now(),
        bookId: book.id,
        title: noteForm.title,
        content: noteForm.content,
        page: noteForm.page ? parseInt(noteForm.page) : undefined,
        createdDate: now
      };
      setNotes(prev => [newNote, ...prev]);
    }

    resetForm();
  };

  const handleEditNote = (note: Note) => {
    setEditingNote(note);
    setNoteForm({
      title: note.title,
      content: note.content,
      page: note.page ? note.page.toString() : ''
    });
    setIsDialogOpen(true);
  };

  const handleDeleteNote = (noteId: number) => {
    setNotes(prev => prev.filter(note => note.id !== noteId));
  };

  const resetForm = () => {
    setNoteForm({ title: '', content: '', page: '' });
    setEditingNote(null);
    setIsDialogOpen(false);
  };

  const openNewNoteDialog = () => {
    resetForm();
    setIsDialogOpen(true);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 뒤로가기 버튼 */}
      <Button 
        variant="ghost" 
        onClick={() => onNavigate(`/bookmark/${bookId}`)}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        책 상세로 돌아가기
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        {/* 책 정보 */}
        <div className="lg:col-span-1">
          <Card>
            <CardContent className="p-6">
              <div className="text-center">
                <ImageWithFallback
                  src={`https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=150&h=225&fit=crop&crop=center&sig=${book.id}`}
                  alt={book.title}
                  className="w-32 h-48 object-cover rounded mx-auto mb-4"
                />
                <h2 className="text-lg mb-2">{book.title}</h2>
                <p className="text-sm text-muted-foreground mb-2">{book.author}</p>
                <Badge variant="secondary">{book.category}</Badge>
              </div>
              
              <div className="mt-6 pt-6 border-t">
                <div className="text-center">
                  <div className="text-2xl mb-1">{bookNotes.length}</div>
                  <p className="text-sm text-muted-foreground">개의 노트</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 노트 목록 */}
        <div className="lg:col-span-3">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h1 className="text-3xl mb-2">독서 노트</h1>
              <p className="text-muted-foreground">
                읽으면서 중요하다고 생각하는 내용을 기록해보세요
              </p>
            </div>
            
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button onClick={openNewNoteDialog}>
                  <Plus className="h-4 w-4 mr-2" />
                  새 노트
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-2xl">
                <DialogHeader>
                  <DialogTitle>
                    {editingNote ? '노트 수정' : '새 노트 작성'}
                  </DialogTitle>
                  <DialogDescription>
                    {book.title}에 대한 독서 노트를 작성하세요
                  </DialogDescription>
                </DialogHeader>
                
                <div className="space-y-4">
                  <div className="grid grid-cols-4 gap-4">
                    <div className="col-span-3">
                      <Label htmlFor="title">제목 *</Label>
                      <Input
                        id="title"
                        value={noteForm.title}
                        onChange={(e) => setNoteForm({...noteForm, title: e.target.value})}
                        placeholder="노트 제목을 입력하세요"
                      />
                    </div>
                    <div>
                      <Label htmlFor="page">페이지</Label>
                      <Input
                        id="page"
                        type="number"
                        min="1"
                        max={book.totalPages}
                        value={noteForm.page}
                        onChange={(e) => setNoteForm({...noteForm, page: e.target.value})}
                        placeholder="페이지"
                      />
                    </div>
                  </div>
                  
                  <div>
                    <Label htmlFor="content">내용 *</Label>
                    <Textarea
                      id="content"
                      value={noteForm.content}
                      onChange={(e) => setNoteForm({...noteForm, content: e.target.value})}
                      placeholder="노트 내용을 입력하세요"
                      rows={8}
                      className="resize-none"
                    />
                  </div>
                  
                  <div className="flex justify-end space-x-2 pt-4">
                    <Button variant="outline" onClick={resetForm}>
                      <X className="h-4 w-4 mr-2" />
                      취소
                    </Button>
                    <Button 
                      onClick={handleSaveNote}
                      disabled={!noteForm.title.trim() || !noteForm.content.trim()}
                    >
                      <Save className="h-4 w-4 mr-2" />
                      저장
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          </div>

          {/* 검색 */}
          <div className="mb-6">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
              <Input
                placeholder="노트 제목이나 내용으로 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>

          {/* 노트 목록 */}
          {filteredNotes.length === 0 ? (
            <Card>
              <CardContent className="p-12 text-center">
                <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg mb-2">
                  {searchTerm ? '검색 결과가 없습니다' : '아직 작성한 노트가 없습니다'}
                </h3>
                <p className="text-muted-foreground mb-4">
                  {searchTerm ? '다른 키워드로 검색해보세요' : '책을 읽으면서 중요한 내용을 노트로 남겨보세요'}
                </p>
                {!searchTerm && (
                  <Button onClick={openNewNoteDialog}>
                    첫 번째 노트 작성하기
                  </Button>
                )}
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {filteredNotes.map((note) => (
                <Card key={note.id}>
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <CardTitle className="text-lg">{note.title}</CardTitle>
                          {note.page && (
                            <Badge variant="outline">{note.page}쪽</Badge>
                          )}
                        </div>
                        <CardDescription>
                          {note.createdDate}
                          {note.updatedDate && note.updatedDate !== note.createdDate && 
                            ` (수정: ${note.updatedDate})`
                          }
                        </CardDescription>
                      </div>
                      <div className="flex space-x-2">
                        <Button 
                          variant="ghost" 
                          size="sm"
                          onClick={() => handleEditNote(note)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="sm"
                          onClick={() => handleDeleteNote(note.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="text-muted-foreground whitespace-pre-wrap leading-relaxed">
                      {note.content}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
})