package com.ecommerce.dto.response;

import com.ecommerce.entity.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryVO {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private List<CategoryVO> children;

    public static CategoryVO fromEntity(Category c) {
        CategoryVO vo = new CategoryVO();
        vo.id = c.getId();
        vo.name = c.getName();
        vo.parentId = c.getParentId();
        vo.sortOrder = c.getSortOrder();
        vo.status = c.getStatus();
        vo.children = new ArrayList<>();
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<CategoryVO> getChildren() { return children; }
    public void setChildren(List<CategoryVO> children) { this.children = children; }
}
