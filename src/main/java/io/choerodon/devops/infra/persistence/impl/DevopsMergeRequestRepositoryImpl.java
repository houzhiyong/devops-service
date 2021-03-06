package io.choerodon.devops.infra.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import io.choerodon.devops.infra.common.util.DateUtil;
import io.choerodon.devops.infra.common.util.enums.MergeRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsMergeRequestE;
import io.choerodon.devops.domain.application.repository.DevopsMergeRequestRepository;
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO;
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;


@Service
public class DevopsMergeRequestRepositoryImpl implements DevopsMergeRequestRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsMergeRequestRepositoryImpl.class);

    @Autowired
    DevopsMergeRequestMapper devopsMergeRequestMapper;

    @Override
    public Integer create(DevopsMergeRequestE devopsMergeRequestE) {
        DevopsMergeRequestDO devopsMergeRequestDO = ConvertHelper.convert(devopsMergeRequestE,
                DevopsMergeRequestDO.class);
        return devopsMergeRequestMapper.insert(devopsMergeRequestDO);
    }

    @Override
    public List<DevopsMergeRequestE> getBySourceBranch(String sourceBranchName, Long gitLabProjectId) {
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO();
        devopsMergeRequestDO.setSourceBranch(sourceBranchName);
        devopsMergeRequestDO.setProjectId(gitLabProjectId);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Sort sort = new Sort(orders);
        return ConvertHelper.convertList(PageHelper.doSort(sort, () ->
                devopsMergeRequestMapper.select(devopsMergeRequestDO)), DevopsMergeRequestE.class);
    }

    @Override
    public DevopsMergeRequestE queryByAppIdAndGitlabId(Long projectId, Long gitlabMergeRequestId) {
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO();
        devopsMergeRequestDO.setProjectId(projectId);
        devopsMergeRequestDO.setGitlabMergeRequestId(gitlabMergeRequestId);
        return ConvertHelper.convert(devopsMergeRequestMapper
                .selectOne(devopsMergeRequestDO), DevopsMergeRequestE.class);
    }

    @Override
    public Page<DevopsMergeRequestE> getMergeRequestList(Integer gitlabProjectId, String state, PageRequest pageRequest) {
        Page<Object> page = PageHelper.doPageAndSort(pageRequest, () ->
                devopsMergeRequestMapper.getByProjectIdAndState(gitlabProjectId, state));
        return ConvertPageHelper.convertPage(page, DevopsMergeRequestE.class);
    }

    @Override
    public Page<DevopsMergeRequestE> getByGitlabProjectId(Integer gitlabProjectId, PageRequest pageRequest) {
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO();
        devopsMergeRequestDO.setProjectId((long) gitlabProjectId);
        Page<Object> page = PageHelper.doPageAndSort(pageRequest, () -> devopsMergeRequestMapper
                .select(devopsMergeRequestDO));
        return ConvertPageHelper.convertPage(page, DevopsMergeRequestE.class);
    }

    @Override
    public List<DevopsMergeRequestE> getByGitlabProjectId(Integer gitlabProjectId) {
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO();
        devopsMergeRequestDO.setProjectId((long) gitlabProjectId);
        List<DevopsMergeRequestDO> devopsMergeRequestDOs = devopsMergeRequestMapper
                .select(devopsMergeRequestDO);
        return ConvertHelper.convertList(devopsMergeRequestDOs, DevopsMergeRequestE.class);
    }

    @Override
    public Integer update(DevopsMergeRequestE devopsMergeRequestE) {
        DevopsMergeRequestDO devopsMergeRequestDO = ConvertHelper
                .convert(devopsMergeRequestE, DevopsMergeRequestDO.class);
        return devopsMergeRequestMapper.updateByPrimaryKey(devopsMergeRequestDO);
    }

    @Override
    public void saveDevopsMergeRequest(DevopsMergeRequestE devopsMergeRequestE) {
        Long projectId = devopsMergeRequestE.getProjectId();
        Long gitlabMergeRequestId = devopsMergeRequestE.getGitlabMergeRequestId();
        DevopsMergeRequestE mergeRequestETemp = queryByAppIdAndGitlabId(projectId, gitlabMergeRequestId);
        Long mergeRequestId = mergeRequestETemp != null ? mergeRequestETemp.getId() : null;
        // 当创建merge request和关闭merge request的时候，webhook发送的时间是本地时间而不是UTC时间，不需要特殊处理
        if (mergeRequestId == null) {
            try {
                create(devopsMergeRequestE);
            } catch (Exception e) {
                LOGGER.info("error.save.merge.request");
            }
        } else {
            // 当合并merge request，即状态变为merge的时候，时区时间是UTC时间。需要特殊处理
            if (MergeRequestStatus.MERGED.value().equals(devopsMergeRequestE.getState())) {
                devopsMergeRequestE.setUpdatedAt(DateUtil.convertUTC2Local(devopsMergeRequestE.getUpdatedAt()));
                devopsMergeRequestE.setCreatedAt(DateUtil.convertUTC2Local(devopsMergeRequestE.getCreatedAt()));
            }

            devopsMergeRequestE.setId(mergeRequestId);
            devopsMergeRequestE.setObjectVersionNumber(mergeRequestETemp.getObjectVersionNumber());
            if (update(devopsMergeRequestE) == 0) {
                throw new CommonException("error.update.merge.request");
            }
        }
    }
}
