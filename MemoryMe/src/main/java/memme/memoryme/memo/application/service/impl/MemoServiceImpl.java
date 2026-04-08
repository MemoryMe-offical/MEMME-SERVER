package memme.memoryme.memo.application.service.impl;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.memo.api.dto.memo.NewMemoDto;
import memme.memoryme.memo.api.dto.memo.MemoDto;
import memme.memoryme.memo.api.dto.post.PostDto;
import memme.memoryme.memo.application.port.UserReader;
import memme.memoryme.memo.application.service.MemoService;
import memme.memoryme.memo.domain.Memo;
import memme.memoryme.memo.domain.Post;
import memme.memoryme.memo.exception.MemoErrorCode;
import memme.memoryme.memo.infra.repository.MemoRepository;
import memme.memoryme.memo.infra.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemoServiceImpl implements MemoService {
    private final MemoRepository memoRepository;
    private final PostRepository postRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserReader userReader;

    @Override
    @Transactional
    public MemoDto createMemo(NewMemoDto newMemoDto) {
        UUID userUid = currentUserProvider.getUid();

        validateUserExists(userUid);

        Memo memo = memoRepository.save(
                Memo.builder()
                        .uid(UUID.randomUUID())
                        .userUid(userUid)
                        .title(newMemoDto.title())
                        .build()
        );

        return toMemoDto(memo);
    }

    @Override
    @Transactional
    public MemoDto updateMemo(MemoDto memoDto) {
        Memo memo = memoRepository.findByUid(memoDto.uid())
                .orElseThrow(() -> new BusinessException(MemoErrorCode.MEMO_NOT_FOUND));

        updateMemoPost(memo, memoDto.post());

        return toMemoDto(memo);
    }

    @Transactional
    public void deleteMemo(UUID noteUid) {
        memoRepository.deleteByUid(noteUid);
    }

    // Page || Slice 고민중
    public List<MemoDto> getUserMemo() {
        return List.of();
    }

    /*----private 메소드----*/

    private void updateMemoPost(Memo memo, PostDto postDto) {
        if (postDto == null) {
            memo.setPost(null);
            return;
        }

        Post post = getOrCreatePost(memo);
        updatePost(post, postDto);
    }

    private Post getOrCreatePost(Memo memo) {
        if (memo.getPost() != null) {
            return memo.getPost();
        }

        Post newPost = postRepository.save(new Post());
        memo.setPost(newPost);
        return newPost;
    }

    private MemoDto toMemoDto(Memo memo) {
        return MemoDto.from(memo);
    }

    private void updatePost(Post post, PostDto postDto) {
        post.setContent(postDto.content());
        post.setImages(postDto.images());
        post.setFiles(postDto.files());
    }

    private void validateUserExists(UUID userUid) {
        if (!userReader.existsByUid(userUid)) {
            throw new BusinessException(MemoErrorCode.USER_UID_NOT_FOUND);
        }
    }
}
