package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.dto.DevopsClusterRepDTO;
import io.choerodon.devops.api.dto.DevopsClusterReqDTO;
import io.choerodon.devops.api.dto.ProjectDTO;
import io.choerodon.devops.app.service.DevopsClusterService;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/clusters")
public class DevopsClusterController {

    @Autowired
    DevopsClusterService devopsClusterService;

    /**
     * 组织下创建集群
     *
     * @param organizationId      组织Id
     * @param devopsClusterReqDTO 集群信息
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "组织下创建集群")
    @PostMapping
    public ResponseEntity<String> create(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群信息", required = true)
            @RequestBody DevopsClusterReqDTO devopsClusterReqDTO) {
        return Optional.ofNullable(devopsClusterService.createCluster(organizationId, devopsClusterReqDTO))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.devops.cluster.insert"));
    }

    /**
     * 更新集群下的项目
     *
     * @param organizationId      组织Id
     * @param devopsClusterReqDTO 集群对象
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "更新集群下的项目")
    @PutMapping
    public void update(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id")
            @RequestParam Long clusterId,
            @ApiParam(value = "集群对象")
            @RequestBody DevopsClusterReqDTO devopsClusterReqDTO) {
        devopsClusterService.updateCluster(clusterId, devopsClusterReqDTO);
    }

    /**
     * 查询单个集群信息
     *
     * @param organizationId 组织Id
     * @param clusterId      集群Id
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "查询单个集群信息")
    @GetMapping("/{clusterId}")
    public ResponseEntity<DevopsClusterRepDTO> query(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id")
            @PathVariable Long clusterId) {
        return Optional.ofNullable(devopsClusterService.getCluster(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cluster.query"));
    }

    /**
     * 校验集群名唯一性
     *
     * @param organizationId 项目id
     * @param name           集群name
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "校验集群名唯一性")
    @GetMapping(value = "/check_name")
    public void checkName(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群name", required = true)
            @RequestParam String name) {
        devopsClusterService.checkName(organizationId, name);
    }

    /**
     * 校验集群编码唯一性
     *
     * @param organizationId 项目id
     * @param code           集群code
     */
    @Permission(level = ResourceLevel.ORGANIZATION, roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "校验集群名唯一性")
    @GetMapping(value = "/check_code")
    public void checkCode(
            @ApiParam(value = "组织Id", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群code", required = true)
            @RequestParam String code) {
        devopsClusterService.checkCode(organizationId, code);
    }

    /**
     * 分页查询项目列表
     *
     * @param organizationId 项目id
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "分页查询项目列表")
    @CustomPageRequest
    @PostMapping("/page_projects")
    public ResponseEntity<Page<ProjectDTO>> pageProjects(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "集群Id")
            @RequestParam(required = false) Long clusterId,
            @ApiParam(value = "模糊搜索参数")
            @RequestBody String[] params) {
        return Optional.ofNullable(devopsClusterService.listProjects(organizationId, clusterId, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }

    /**
     * 查询已有权限的项目列表
     *
     * @param organizationId 项目id
     * @return List
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "查询已有权限的项目列表")
    @GetMapping("/list_cluster_projects/{clusterId}")
    public ResponseEntity<List<ProjectDTO>> listClusterProjects(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id")
            @PathVariable Long clusterId) {
        return Optional.ofNullable(devopsClusterService.listClusterProjects(organizationId, clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.project.query"));
    }


    /**
     * 查询shell脚本
     *
     * @param organizationId 组织ID
     * @param clusterId      集群Id
     * @return String
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "查询shell脚本")
    @CustomPageRequest
    @GetMapping("/query_shell/{clusterId}")
    public ResponseEntity<String> queryShell(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id", required = true)
            @PathVariable Long clusterId) {
        return Optional.ofNullable(devopsClusterService.queryShell(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cluster.query"));
    }

    /**
     * 集群列表查询
     *
     * @param organizationId 组织ID
     * @return Page
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "集群列表查询")
    @CustomPageRequest
    @PostMapping("/page_cluster")
    public ResponseEntity<Page<DevopsClusterRepDTO>> listCluster(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "分页参数")
            @ApiIgnore PageRequest pageRequest,
            @ApiParam(value = "是否需要分页")
            @RequestParam(value = "doPage", required = false) Boolean doPage,
            @ApiParam(value = "查询参数")
            @RequestBody String params) {
        return Optional.ofNullable(devopsClusterService.pageClusters(organizationId, doPage, pageRequest, params))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cluster.query"));
    }

    /**
     * 删除集群
     *
     * @param organizationId 组织ID
     * @param clusterId      集群Id
     * @return String
     */
    @Permission(level = ResourceLevel.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR})
    @ApiOperation(value = "删除集群")
    @CustomPageRequest
    @DeleteMapping("/{clusterId}")
    public ResponseEntity<String> deleteCluster(
            @ApiParam(value = "组织ID", required = true)
            @PathVariable(value = "organization_id") Long organizationId,
            @ApiParam(value = "集群Id")
            @PathVariable Long clusterId) {
        return Optional.ofNullable(devopsClusterService.deleteCluster(clusterId))
                .map(target -> new ResponseEntity<>(target, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.cluster.delete"));
    }
}
