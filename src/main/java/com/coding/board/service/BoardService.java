package com.coding.board.service;

import com.coding.board.dto.BoardDTO;
import com.coding.board.entity.BoardEntity;
import com.coding.board.entity.BoardFileEntity;
import com.coding.board.repository.BoardFileRepository;
import com.coding.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;

    public void save(BoardDTO boardDTO) throws IOException {
        // 파일 첨부 여부에 따라 로직을 분리함
        if(boardDTO.getBoardFile().isEmpty()){
            //첨부파일 없을경우
            BoardEntity boardEntity = BoardEntity.toSavaEntity(boardDTO);
            boardRepository.save(boardEntity);
        } else {
            /*파일있는경우(dto에 담긴 파일을 꺼냄, 파일 이름 가져옴,
             서버 저장용 이름 만들기,
             저장 경로 설정, 해당 경로에 파일 저장,
             board_table에 데이터 save 처리, board_file_table에 해당 데이터 save 처리)*/

            //1. dto에 담긴 파일을 꺼냄
             MultipartFile boardFile = boardDTO.getBoardFile();
            //2. 파일 이름을 가져옴
            String originalFilename = boardFile.getOriginalFilename();
            //3. 서버저장용 이름 만들기->342782472_사진.jpg
            String storedFileName = System.currentTimeMillis() + "_" + originalFilename;
            //4. 저장 경로설정정
            String savePath = "C:/springboot_img/" + storedFileName;
            //5. 해당 경로에 파일 저장
            boardFile.transferTo(new File(savePath));
            //6.board_table에 데이터 save 처리
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedId = boardRepository.save(boardEntity).getId();
            BoardEntity board = boardRepository.findById(savedId).get();


            BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(board, originalFilename, storedFileName);
            boardFileRepository.save(boardFileEntity);  //db에 저장까지


        }

    }

    @Transactional
    public List<BoardDTO> findAll() {
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        List<BoardDTO> boardDTOList = new ArrayList<>();
        for (BoardEntity boardEntity: boardEntityList) {
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList;

    }
    @Transactional
    public void updateHits(Long id) {
        boardRepository.updateHits(id);

    }

    @Transactional
    public BoardDTO findById(Long id) {
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if (optionalBoardEntity.isPresent()) {
            BoardEntity boardEntity = optionalBoardEntity.get();
            BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity);
            return boardDTO;
        } else {
            return null;
        }
    }


    public BoardDTO update(BoardDTO boardDTO) {
        BoardEntity boardEntity = BoardEntity.toUpdateEntity(boardDTO);
        boardRepository.save(boardEntity);
        return findById(boardDTO.getId());
    }

    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    public Page<BoardDTO> paging(Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 3; //한 페이지에 보여주는 글 갯수
        // page 위치에 있는 값이 0부터 시작함
        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit,
                        Sort.by(Sort.Direction.DESC, "id")));   // Page 객체로 받고 id 기준으로 내림차순으로 정렬

        System.out.println("boardEntities.getContent() = " + boardEntities.getContent()); // 요청 페이지에 해당하는 글
        System.out.println("boardEntities.getTotalElements() = " + boardEntities.getTotalElements()); // 전체 글갯수
        System.out.println("boardEntities.getNumber() = " + boardEntities.getNumber()); // DB로 요청한 페이지 번호
        System.out.println("boardEntities.getTotalPages() = " + boardEntities.getTotalPages()); // 전체 페이지 갯수
        System.out.println("boardEntities.getSize() = " + boardEntities.getSize()); // 한 페이지에 보여지는 글 갯수
        System.out.println("boardEntities.hasPrevious() = " + boardEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("boardEntities.isFirst() = " + boardEntities.isFirst()); // 첫 페이지 여부
        System.out.println("boardEntities.isLast() = " + boardEntities.isLast()); // 마지막 페이지 여부

        // map method는 board에서 뱐수를 꺼내서 BoardDTO로 하나씩 옮겨줌(엔티티를 board 객체로 바꿔줌)
        Page<BoardDTO> boardDTOS = boardEntities.map(board -> new BoardDTO(board.getId(), board.getBoardWriter(),
                board.getBoardTitle(), board.getBoardHits(), board.getCreatedTime()));

        return boardDTOS;



    }
}
