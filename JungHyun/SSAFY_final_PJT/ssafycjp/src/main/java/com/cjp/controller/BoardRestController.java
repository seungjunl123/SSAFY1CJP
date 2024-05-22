package com.cjp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cjp.model.dto.Board;
import com.cjp.model.dto.Reply;
import com.cjp.model.dto.Rereply;
import com.cjp.model.dto.SearchCondition;
import com.cjp.model.service.BoardService;
import com.cjp.model.service.ReplyService;
import com.cjp.model.service.RereplyService;
import com.cjp.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api-board")
@Tag(name = "BoardRestController", description = "게시판 CRUD")
public class BoardRestController {
	private final BoardService boardService;
	private final ReplyService replyService;
	private final RereplyService rereplyService;

	@Autowired
	public JwtUtil jwtUtil;

	@Autowired
	public BoardRestController(BoardService boardService, ReplyService replyService, RereplyService rereplyService) {
		this.boardService = boardService;
		this.replyService = replyService;
		this.rereplyService = rereplyService;
	}

	// 게시글 전체 조회
	@GetMapping("/board")
	public ResponseEntity<?> list() {
		List<Board> list = boardService.getBoardList(); // 전체조회
		return new ResponseEntity<List<Board>>(list, HttpStatus.OK);
	}

	// 게시판 id에 해당하는 게시글 조회
	@GetMapping("/board/{postBoardId}")
	public ResponseEntity<?> getBoardsByPostBoardId(@PathVariable("postBoardId") int postBoardId) {
		List<Board> list = boardService.getBoardListByPostBoardId(postBoardId);
		return new ResponseEntity<List<Board>>(list, HttpStatus.OK);
	}

	// 게시판 이름 목록 조회
	@GetMapping("/board/postboardnames")
	public ResponseEntity<List<String>> getPostboardNames() {
		List<String> postboardNames = boardService.getNames();
		return new ResponseEntity<>(postboardNames, HttpStatus.OK);
	}
	
	// 해당 게시판의 게시글 말머리 리스트 가져오기
	@GetMapping("/board/categories/{postBoardId}")
	public ResponseEntity<List<Category>> getCategoriesByPostBoardId(@PathVariable("postBoardId") int postBoardId) {
	    List<Category> categories = boardService.getCategoriesByPostBoardId(postBoardId);
	    return new ResponseEntity<List<Category>>(categories, HttpStatus.OK);
	}

	// 게시글 (검색) 조회
	@GetMapping("/board/search")
	@Operation(summary = "게시글 조회", description = "게시글 조건에 따른 조회 가넝")
	public ResponseEntity<?> list(@Parameter(description = "검색 조건") @RequestBody SearchCondition condition) {
		List<Board> list = boardService.search(condition);

		if (list == null || list.size() == 0) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Board>>(list, HttpStatus.OK);
	}

	// 게시글 상세 보기
	@GetMapping("/board/detail/{id}")
	public ResponseEntity<Board> detail(@PathVariable("id") int id) {
		Board board = boardService.readBoard(id);
		return new ResponseEntity<Board>(board, HttpStatus.OK);
	}

	// 게시글 등록 (Form 형식으로 넘어왔을 때)
	@PostMapping("/board")
	public ResponseEntity<?> write(@RequestBody Board board, @RequestHeader("Authorization") String token) {
		// 등록한 게시글을 보냈는데
		String userId = jwtUtil.getIdFromToken(token);
		board.setWriter(userId);
		boardService.writeBoard(board);
		// 등록이 되어있는지 눈으로 Talend API 보려고 이렇게 보낸거지
		// 실제로 프론트에게 보낼때는 크게 의미는 없다! ID만 보내서 디테일 쏘던지 바로 목록으로가면 필요없다!
		// insert, update, delete -> 반환값이 int 형의 값이 넘어온다. (바뀐 행의 개수가 반환됨)
		return new ResponseEntity<Board>(board, HttpStatus.CREATED);
	}

	// 게시글 수정 (JSON형태로 보낸다)
	@PutMapping("/board/{id}")
	public ResponseEntity<Void> update(@PathVariable("id") int id, @RequestBody Board board) {
		board.setId(id);
		boardService.modifyBoard(board); // id를 따로 보내왔다면 바구니(DTO)에 넣어놓고 보내자
		return new ResponseEntity<Void>(HttpStatus.OK); // 조금 더 세밀하게 컨트롤 할 수도 있다.
	}

	// 게시글 삭제
	@DeleteMapping("/board/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") int id) {
		boardService.removeBoard(id);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	// 댓글 가져오기
	@GetMapping("/board/{boardId}/reply")
	public ResponseEntity<List<Reply>> replyList(@PathVariable("boardId") int boardId) {
		List<Reply> list = replyService.getReplyList(boardId);
		return new ResponseEntity<List<Reply>>(list, HttpStatus.OK);
	}

	// 댓글 작성
	@PostMapping("/board/{boardId}/reply")
	public ResponseEntity<?> writeReply(@RequestBody Reply reply) {
		replyService.writeReply(reply);
		return new ResponseEntity<Reply>(reply, HttpStatus.OK);
	}

	// 댓글 수정
	@PutMapping("/board/{boardId}/reply/{id}")
	public ResponseEntity<?> updateReply(@PathVariable("id") int id, @RequestBody Reply reply) {
		reply.setId(id);
		replyService.modifyReply(reply);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	// 댓글 삭제
	@DeleteMapping("/board/{boardId}/reply/{id}")
	public ResponseEntity<?> deleteReply(@PathVariable("id") int id) {
		replyService.removeReply(id);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	// 대댓글 가져오기
	@GetMapping("/board/{boardId}/reply/{replyId}/rereply")
	public ResponseEntity<List<Rereply>> rereplyList(@PathVariable("replyId") int replyId) {
		List<Rereply> list = rereplyService.getRereplyList(replyId);
		return new ResponseEntity<List<Rereply>>(list, HttpStatus.OK);
	}

	// 대댓글 작성
	@PostMapping("/board/{boardId}/reply/{replyId}/rereply")
	public ResponseEntity<?> writeRereply(@RequestBody Rereply rereply) {
		rereplyService.writeRereply(rereply);
		return new ResponseEntity<Rereply>(rereply, HttpStatus.OK);
	}

	// 대댓글 수정
	@PutMapping("/board/{boardId}/reply/{replyId}/rereply/{id}")
	public ResponseEntity<?> updateRereply(@PathVariable("id") int id, @RequestBody Rereply rereply) {
		rereply.setId(id);
		rereplyService.modifyRereply(rereply);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	// 대댓글 삭제
	@DeleteMapping("/board/{boardId}/reply/{replyId}/rereply/{id}")
	public ResponseEntity<?> deleteRereply(@PathVariable("id") int id) {
		rereplyService.removeRereply(id);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	// 게시글 좋아요
	@PutMapping("board/{id}/like")
	public ResponseEntity<?> boardLikeUp(@PathVariable("id") int boardId,
			@RequestHeader("Authorization") String token) {
		String UserId = jwtUtil.getIdFromToken(token);
		boardService.updateLikeUp(UserId, boardId);

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	// 게시글 좋아요 눌렀는지 확인 (boolean)
	@GetMapping("board/{id}/liked")
	public ResponseEntity<?> checkIfUserLikedBoard(@PathVariable("id") int boardId,
			@RequestHeader("Authorization") String token) {
		String UserId = jwtUtil.getIdFromToken(token);
		boolean result = boardService.checkBoardLiked(UserId, boardId);

		return new ResponseEntity<Boolean>(result, HttpStatus.OK);
	}

	// 게시글 좋아요 취소
	@PutMapping("board/{id}/dislike")
	public ResponseEntity<?> boardLikeDown(@PathVariable("id") int boardId,
			@RequestHeader("Authorization") String token) {
		String UserId = jwtUtil.getIdFromToken(token);
		boardService.updateLikeDown(UserId, boardId);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	// 댓글 좋아요
	@PutMapping("/board/{boardId}/reply/{replyId}/like")
	public ResponseEntity<?> replyLikeUp(@PathVariable("replyId") int replyId,
			@RequestHeader("Authorization") String token) {
		String UserId = jwtUtil.getIdFromToken(token);
		replyService.updateLikeUp(UserId, replyId);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	// 댓글 좋아요 취소
	@PutMapping("/board/{boardId}/reply/{replyId}/dislike")
	public ResponseEntity<?> replyLikeDown(@PathVariable("replyId") int replyId,
			@RequestHeader("Authorization") String token) {
		String UserId = jwtUtil.getIdFromToken(token);
		replyService.updateLikeDown(UserId, replyId);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
