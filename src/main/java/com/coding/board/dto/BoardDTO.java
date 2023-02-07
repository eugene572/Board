package com.coding.board.dto;

import com.coding.board.entity.BoardEntity;
import com.coding.board.entity.BoardFileEntity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {

    private Long id;
    private String boardWriter;
    private String boardPass;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    private LocalDateTime boardCreatedTime;
    private LocalDateTime boardUpdatedTime;

    private MultipartFile boardFile;    // save.html -> Controller 파일담는 용도
    private String originalFileName;    // 원본 파일 이름
    private String storedFileName;  // 서버 저장용 파일 이름
    private int fileAttached;   // 파일 첨부 여부(첨부1, 미첨부0)

    // 페이징 처리를 위해 전달할 것
    public BoardDTO(Long id, String boardWriter, String boardTitle, int boardHits, LocalDateTime boardCreatedTime) {
        this.id = id;
        this.boardWriter = boardWriter;
        this.boardTitle = boardTitle;
        this.boardHits = boardHits;
        this.boardCreatedTime = boardCreatedTime;
    }

    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(boardEntity.getId());
        boardDTO.setBoardWriter(boardEntity.getBoardWriter());
        boardDTO.setBoardPass(boardEntity.getBoardPass());
        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
        boardDTO.setBoardContents(boardEntity.getBoardContents());
        boardDTO.setBoardHits(boardEntity.getBoardHits());
        boardDTO.setBoardCreatedTime(boardEntity.getCreatedTime());
        boardDTO.setBoardUpdatedTime(boardEntity.getUpdatedTime());
        if (boardEntity.getFileAttached() == 0) {
            boardDTO.setFileAttached(boardEntity.getFileAttached()); // 0
        } else {
            /*List<String> originalFileNameList = new ArrayList<>();
            List<String> storedFileNameList = new ArrayList<>();*/

            boardDTO.setFileAttached(boardEntity.getFileAttached());    // 1
            //for(BoardFileEntity boardFileEntity: boardEntity.getBoardFileEntityList()){
            //파일 이름을 가져가야 함.
            //origianl , stored 파일은 board_file_table에 저장되어있음
            //join(select * from board_table b, board_file_table bf where b.id=bf.board_id and where b.id=?
            /*originalFileNameList.add(boardFileEntity.getOriginalFileName());
            storedFileNameList.add(boardFileEntity.getOriginalFileName());*/

            boardDTO.setOriginalFileName(boardEntity.getBoardFileEntityList().get(0).getOriginalFileName());
            boardDTO.setStoredFileName(boardEntity.getBoardFileEntityList().get(0).getStoredFileName());
        }

        return boardDTO;
    }

}
