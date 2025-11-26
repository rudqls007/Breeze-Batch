package com.example.kybatch.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


/*
* @Entity 로 JPA 엔티티 등록
* @Table("users") 명시 — 테이블명을 명확하게
* 필수값은 nullable = false
* 이메일은 unique = true 로 중복 방지
* Batch에서 Reader가 이 데이터를 읽어 CSV 만들 예정
* */
@Getter
@NoArgsConstructor
@Entity
@Table(name ="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /* 상태값
    *  ACTIVE / INACTIVE */
    @Column(nullable = false)
    private String status;

    public User(String name, String email, String status) {
        this.name = name;
        this.email = email;
        this.status = status;
    }

    public void updateStatus(String status) {
        this.status = status;

    }

    public void updateName(String name){
        this.name = name;
    }
}
