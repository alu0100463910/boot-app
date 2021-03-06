package com.myapp.controller

import com.myapp.domain.Relationship
import com.myapp.domain.User
import com.myapp.repository.RelationshipRepository
import com.myapp.repository.UserRepository
import com.myapp.service.SecurityContextService
import org.springframework.beans.factory.annotation.Autowired

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RelationshipControllerTest extends BaseControllerTest {

    @Autowired
    UserRepository userRepository

    @Autowired
    RelationshipRepository relationshipRepository

    SecurityContextService securityContextService = Mock(SecurityContextService)

    @Override
    def controllers() {
        return new RelationshipController(userRepository, relationshipRepository, securityContextService)
    }

    def "can follow another user"() {
        given:
        User follower = userRepository.save(new User(username: "test1@test.com", password: "secret", name: "test"))
        User followed = userRepository.save(new User(username: "test2@test.com", password: "secret", name: "test"))
        securityContextService.currentUser() >> follower

        when:
        def response = perform(post("/api/relationships/to/${followed.id}"))

        then:
        response.andExpect(status().isOk())
        relationshipRepository.count() == 1
    }

    def "can unfollow another user"() {
        given:
        User follower = userRepository.save(new User(username: "test1@test.com", password: "secret", name: "test"))
        User followed = userRepository.save(new User(username: "test2@test.com", password: "secret", name: "test"))
        relationshipRepository.save(new Relationship(follower: follower, followed: followed))
        securityContextService.currentUser() >> follower

        when:
        def response = perform(delete("/api/relationships/to/${followed.id}"))

        then:
        response.andExpect(status().isOk())
        relationshipRepository.count() == 0

        when:
        response = perform(delete("/api/relationships/to/${followed.id}"))

        then:
        response.andExpect(status().isNotFound())
    }

}
