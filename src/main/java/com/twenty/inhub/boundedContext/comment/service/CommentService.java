package com.twenty.inhub.boundedContext.comment.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.twenty.inhub.base.request.RsData;
import com.twenty.inhub.boundedContext.comment.dto.CommentDto;
import com.twenty.inhub.boundedContext.comment.entity.Comment;
import com.twenty.inhub.boundedContext.comment.repository.CommentRepository;
import com.twenty.inhub.boundedContext.member.entity.Member;
import com.twenty.inhub.boundedContext.member.repository.MemberRepository;
import com.twenty.inhub.boundedContext.post.entity.Post;
import com.twenty.inhub.boundedContext.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public RsData createComment(CommentDto commentDto, Member member, Post post) {
        Comment comment = Comment.toSaveEntity(commentDto, member, post);
        Comment savedComment = commentRepository.save(comment);
        CommentDto savedCommentDto = CommentDto.toCommentDto(savedComment);
        return RsData.of("S-60", "댓글이 생성되었습니다.", savedCommentDto);
    }

    public CommentDto getComment(Long id) {
        Comment comment = commentRepository.findById(id).orElse(null);
        return CommentDto.toCommentDto(comment);
    }

    public Long updateComment(Long id, String content) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setContent(content);
            Comment updatedComment = commentRepository.save(comment);
            return updatedComment.getPost().getId();
        }
        return null;
    }

    public RsData deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        Member member = comment.getMember();
        member.getComments().remove(comment);
        memberRepository.save(member); // Member 엔티티 저장

        Post post = comment.getPost();
        post.getComments().remove(comment);
        postRepository.save(post); // Post 엔티티 저장

        commentRepository.deleteById(id); // 댓글 삭제

        return RsData.of("S-62", "댓글이 삭제되었습니다.");
    }
}
