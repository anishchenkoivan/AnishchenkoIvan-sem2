package com.example.demo.service;

import com.example.demo.entity.Tag;
import com.example.demo.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService {
    private final TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public void createTag(String name) {
        tagRepository.save(new Tag(name));
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        tagRepository.delete(tag);
    }

    @Transactional
    public void updateTag(Long id, String name) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        tag.setName(name);
        tagRepository.save(tag);
    }

    @Transactional
    public Tag getTagByName(String name) {
        return tagRepository.findTagByName(name).orElseThrow();
    }
}
