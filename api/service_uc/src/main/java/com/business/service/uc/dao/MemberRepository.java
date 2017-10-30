package com.business.service.uc.dao;

import com.business.service.uc.entity.MemberEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author panyao
 * @date 2017/9/2
 */
@Repository
public interface MemberRepository extends CrudRepository<MemberEntity, Long> {

}