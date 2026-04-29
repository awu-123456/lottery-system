package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.*;
import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.dto.*;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

    private final String ACTIVITY_PREFIX = "ACTIVITY_";

    private final Long ACTIVITY_TIMEOUT = 60 * 60 * 24 * 3L;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private ActivityUserMapper activityUserMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public CreateActivityDTO createActivity(CreateActivityParam param) {
        checkActivityInfo(param);

        ActivityDO activityDO = new ActivityDO();
        activityDO.setActivityName(param.getActivityName());
        activityDO.setDescription(param.getDescription());
        activityDO.setStatus(ActivityStatusEnum.RUNNING.name());
        activityMapper.insert(activityDO);

        List<CreatePrizeByActivityParam> prizeParams = param.getActivityPrizeList();
        List<ActivityPrizeDO> activityPrizeDOList = prizeParams.stream()
                .map(prizeParam -> {
                    ActivityPrizeDO activityPrizeDO = new ActivityPrizeDO();
                    activityPrizeDO.setActivityId(activityDO.getId());
                    activityPrizeDO.setPrizeId(prizeParam.getPrizeId());
                    activityPrizeDO.setPrizeAmount(prizeParam.getAmount());
                    activityPrizeDO.setPrizeTiers(prizeParam.getPrizeTiers());
                    activityPrizeDO.setStatus(ActivityPrizeStatusEnum.INIT.name());
                    return activityPrizeDO;
                }).collect(Collectors.toList());
        activityPrizeMapper.batchInsert(activityPrizeDOList);

        List<CreateUserByActivityParam> userParams = param.getActivityUserList();
        List<ActivityUserDO> activityUserDOList = userParams.stream()
                .map(userParam -> {
                    ActivityUserDO activityUserDo = new ActivityUserDO();
                    activityUserDo.setActivityId(activityDO.getId());
                    activityUserDo.setUserId(userParam.getUserId());
                    activityUserDo.setUserName(userParam.getUserName());
                    activityUserDo.setStatus(ActivityUserStatusEnum.INIT.name());
                    return activityUserDo;
                }).collect(Collectors.toList());
        activityUserMapper.batchInsert(activityUserDOList);

        List<Long> prizeIds = param.getActivityPrizeList().stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .collect(Collectors.toList());
        List<PrizeDO> prizeDOList = prizeMapper.batchSelectByIds(prizeIds);
        ActivityDetailDTO detailDTO = convertToActivityDetailDTO(activityDO, activityUserDOList,activityPrizeDOList,prizeDOList);

        cacheActivity(detailDTO);
        CreateActivityDTO createActivityDTO = new CreateActivityDTO();
        createActivityDTO.setActivityId(activityDO.getId());
        return createActivityDTO;
    }

    @Override
    public PageListDTO<ActivityDTO> findActivityList(PageParam param) {
        Integer total = activityMapper.count();

        List<ActivityDO> activityDOList = activityMapper.selectActivityList(param.offset(),param.getPageSize());
        List<ActivityDTO> activityDTOList = activityDOList.stream()
                .map(activityDO -> {
                    ActivityDTO activityDTO = new ActivityDTO();
                    activityDTO.setActivityId(activityDO.getId());
                    activityDTO.setActivityName(activityDO.getActivityName());
                    activityDTO.setDescription(activityDO.getDescription());
                    activityDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));
                    return activityDTO;
                }).collect(Collectors.toList());

        return new PageListDTO<>(total,activityDTOList);
    }

    @Override
    public ActivityDetailDTO getActivityDetail(Long activityId) {
        if(activityId == null) {
            logger.warn("查询活动详细信息失败，activityId为空！");
            return null;
        }
        ActivityDetailDTO detailDTO = getActivityDetailDTO(activityId);
        if(detailDTO != null) {
            logger.info("查询活动详细信息成功！detailDTO={}", JacksonUtil.writeValueAsString(detailDTO));
            return detailDTO;
        }
        ActivityDO activityDO = activityMapper.selectById(activityId);
        List<ActivityPrizeDO> apDOList = activityPrizeMapper.selectByActivityId(activityId);
        List<ActivityUserDO> auDOList = activityUserMapper.selectByActivityId(activityId);
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<PrizeDO> prizeDOList = prizeMapper.batchSelectByIds(prizeIds);
        detailDTO = convertToActivityDetailDTO(activityDO, auDOList, apDOList,prizeDOList);
        cacheActivity(detailDTO);
        return detailDTO;
    }

    @Override
    public void cacheActivity(Long activityId) {
        if(activityId == null) {
            logger.warn("要缓存的活动id为空！");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_IS_EMPTY);
        }
        ActivityDO activityDO = activityMapper.selectById(activityId);
        if(activityDO == null) {
            logger.error("要缓存的活动id有误！");
            throw new ServiceException(ServiceErrorCodeConstants.CACHE_ACTIVITY_ID_ERROR);
        }
        List<ActivityPrizeDO> apDOList = activityPrizeMapper.selectByActivityId(activityId);
        List<ActivityUserDO> auDOList = activityUserMapper.selectByActivityId(activityId);
        List<Long> prizeIds = apDOList.stream()
                .map(ActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<PrizeDO> prizeDOList = prizeMapper.batchSelectByIds(prizeIds);
        cacheActivity(convertToActivityDetailDTO(activityDO, auDOList, apDOList,prizeDOList));
    }

    private void cacheActivity(ActivityDetailDTO detailDTO) {
        if(detailDTO == null || detailDTO.getActivityId() == null) {
            logger.warn("要缓存的活动信息不存在!");
            return;
        }
        try {
            redisUtil.set(ACTIVITY_PREFIX+detailDTO.getActivityId(),
                    JacksonUtil.writeValueAsString(detailDTO),
                    ACTIVITY_TIMEOUT);
        } catch (Exception e) {
            logger.error("缓存活动异常，ActivityDetailDTO={}", JacksonUtil.writeValueAsString(detailDTO),e);
        }
    }

    private ActivityDetailDTO getActivityDetailDTO(Long activityId) {
        if(activityId == null) {
            logger.warn("获取缓存活动数据的activityId为空！");
            return null;
        }
        try {
            String str = redisUtil.get(ACTIVITY_PREFIX+activityId);
            if(!StringUtils.hasText(str)) {
                logger.info("获取的缓存活动数据为空！key={}", ACTIVITY_PREFIX+activityId);
                return null;
            }
            return JacksonUtil.readValue(str, ActivityDetailDTO.class);
        } catch (Exception e) {
            logger.error("从缓存中获取活动信息异常，key={}",ACTIVITY_PREFIX+activityId,e);
            return  null;
        }
    }

    private ActivityDetailDTO convertToActivityDetailDTO(ActivityDO activityDO,
                                                         List<ActivityUserDO> activityUserDOList,
                                                         List<ActivityPrizeDO> activityPrizeDOList,
                                                         List<PrizeDO> prizeDOList) {
        ActivityDetailDTO detailDTO = new ActivityDetailDTO();
        detailDTO.setActivityId(activityDO.getId());
        detailDTO.setActivityName(activityDO.getActivityName());
        detailDTO.setDesc(activityDO.getDescription());
        detailDTO.setStatus(ActivityStatusEnum.forName(activityDO.getStatus()));

        List<ActivityDetailDTO.PrizeDTO> prizeDTOList = activityPrizeDOList.stream()
                .map(apDO -> {
                    ActivityDetailDTO.PrizeDTO prizeDTO = new ActivityDetailDTO.PrizeDTO();
                    prizeDTO.setPrizeId(apDO.getPrizeId());
                    Optional<PrizeDO> optionalPrizeDO = prizeDOList.stream()
                            .filter(prizeDO -> prizeDO.getId().equals(apDO.getPrizeId()))
                            .findFirst();
                    optionalPrizeDO.ifPresent(prizeDO -> {
                        prizeDTO.setPrizeId(prizeDO.getId());
                        prizeDTO.setName(prizeDO.getName());
                        prizeDTO.setImageUrl(prizeDO.getImageUrl());
                        prizeDTO.setPrice(prizeDO.getPrice());
                        prizeDTO.setDescription(prizeDO.getDescription());
                    });
                    prizeDTO.setTiers(ActivityPrizeTiersEnum.forName(apDO.getPrizeTiers()));
                    prizeDTO.setPrizeAmount(apDO.getPrizeAmount());
                    prizeDTO.setStatus(ActivityPrizeStatusEnum.forName(apDO.getStatus()));
                    return prizeDTO;
                }).collect(Collectors.toList());
        detailDTO.setPrizeDTOList(prizeDTOList);

        List<ActivityDetailDTO.UserDTO> userDTOList = activityUserDOList.stream()
                .map(auDO -> {
                    ActivityDetailDTO.UserDTO userDTO = new ActivityDetailDTO.UserDTO();
                    userDTO.setUserId(auDO.getUserId());
                    userDTO.setUserName(auDO.getUserName());
                    userDTO.setStatus(ActivityUserStatusEnum.forName(auDO.getStatus()));
                    return userDTO;
                }).collect(Collectors.toList());
        detailDTO.setUserDTOList(userDTOList);
        return detailDTO;
    }

    private void checkActivityInfo(CreateActivityParam param) {
        if(param == null) {
            throw new ServiceException(ServiceErrorCodeConstants.CREATE_ACTIVITY_INFO_IS_EMPTY);
        }

        List<Long> userIds = param.getActivityUserList().stream()
                .map(CreateUserByActivityParam::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> existUserIds = userMapper.selectExistById(userIds);
        if(CollectionUtils.isEmpty(existUserIds)) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
        }
        userIds.forEach(id -> {
            if(!existUserIds.contains(id)) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_USER_ERROR);
            }
        });

        List<Long> prizeIds = param.getActivityPrizeList().stream()
                .map(CreatePrizeByActivityParam::getPrizeId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> existPrizeIds = prizeMapper.selectExistById(prizeIds);
        if(CollectionUtils.isEmpty(existPrizeIds)) {
            throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
        }
        prizeIds.forEach(id -> {
            if(!existPrizeIds.contains(id)) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_ERROR);
            }
        });

        long userAmount = param.getActivityUserList().size();
        long prizeAmount = param.getActivityPrizeList().stream()
                .mapToLong(CreatePrizeByActivityParam::getAmount)
                .sum();
        if(prizeAmount > userAmount) {
            throw new ServiceException(ServiceErrorCodeConstants.USER_PRIZE_AMOUNT_ERROR);
        }

        param.getActivityPrizeList().forEach(prize -> {
            if(ActivityPrizeTiersEnum.forName(prize.getPrizeTiers()) == null) {
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_PRIZE_TIERS_ERROR);
            }
        });
    }
}
