package com.spring_boot.blog_application.service;

import com.spring_boot.blog_application.entity.BlogEntity;
import com.spring_boot.blog_application.entity.User;
import com.spring_boot.blog_application.repo.BlogRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
// @Slf4j
public class BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogService.class);
    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private CloudinaryImageService cloudinaryImageService;

    @Autowired
    UserService userService;

    @Transactional
    public void saveEntry(BlogEntity blogEntry, String userName) {
        try {
            User user = userService.findByUserName(userName);
            blogEntry.setDate(LocalDateTime.now());
            BlogEntity save = blogRepository.save(blogEntry);
            user.getBlogEntries().add(save);
            userService.saveUser(user);
        } catch (Exception e) {

            throw new RuntimeException("An error occured while saving the " +
                    "entry!", e);
        }
    }

    @Transactional
    public void saveEntry(BlogEntity blogEntry, String userName,
            MultipartFile[] files) {
        try {
            // Find the user by username
            User user = userService.findByUserName(userName);

            // Handle image uploads if files are provided
            if (files != null && files.length > 0) {
                List<String> imageUrls = cloudinaryImageService.uploadImages(files);
                blogEntry.setImageUrls(imageUrls);
            }

            // Set the current date and time for the blog entry
            blogEntry.setDate(LocalDateTime.now());

            // Save the blog entry in the repository
            BlogEntity savedEntry = blogRepository.save(blogEntry);

            // Add the saved blog entry to the user's list of blog entries
            user.getBlogEntries().add(savedEntry);

            // Save the updated user object
            userService.saveUser(user);
        } catch (Exception e) {
            // Log the exception (if needed) and throw a runtime exception with a custom
            // message
            throw new RuntimeException("An error occurred while saving the entry!", e);
        }
    }

    public void saveEntry(BlogEntity blogEntry) {
        blogRepository.save(blogEntry);
    }

    public Optional<BlogEntity> blogById(String Id) {
        return blogRepository.findById(Id);
    }

    @Transactional
    public boolean deleteBlogById(String Id, String userName) {
        boolean removed = false;
        try {
            User user = userService.findByUserName(userName);
            removed = user.getBlogEntries().removeIf(x -> x.getId().equals(Id));
            if (removed) {
                userService.saveUser(user);
                blogRepository.deleteById(Id);
            }
        } catch (Exception e) {
            log.error("Error ", e);
            throw new RuntimeException("An error occured while deleting the " +
                    "entry");
        }
        return removed;
    }

    public Optional<BlogEntity> findByImageUrl(String imageUrl) {
        return blogRepository.findByImageUrlsContaining(imageUrl);
    }

    public List<BlogEntity> getBlogEntries() {
        return blogRepository.findAll(); // Assuming you have a findAll method in your repository
    }

}
