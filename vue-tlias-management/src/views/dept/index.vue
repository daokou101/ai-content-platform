<script setup>
import { ref, onMounted, watch } from 'vue';
import { queryAllDeptApi, addDeptApi, queryDeptByIdApi, updateDeptApi, deleteDeptApi } from '@/api/dept'
import { ElMessage, ElMessageBox } from 'element-plus';


//定义钩子函数 - 
onMounted(() => {
  search()
});

//获取部门列表 - search
const search = async () => {
  /* const result = await axios.get('https://apifoxmock.com/m1/4988878-0-default/depts'); */

  const result = await queryAllDeptApi();
  if(result.code){ //成功 ---> 隐式类型转换: 0 , '' , null , undefined ------> false ; 其他都是true
    deptList.value = result.data;
  }
}

// 示例数据
const deptList = ref([]);
  
// 处理编辑操作
const handleEdit = async (id) => {
  console.log('编辑:', id);
  addDialogVisible.value = true; 
  formTitle.value = '编辑部门';
  //重置表单校验数据
  if(addFormRef.value){ //表单存在
    addFormRef.value.resetFields();
  }

  //根据ID查询部门 - 回显
  const result = await queryDeptByIdApi(id);
  if(result.code){
    dept.value = result.data;
  }
}

// 删除操作
function handleDelete(id) {
  console.log('删除:', id);
  
  //弹出消息确认框, 如果点击确定, 执行删除; 如果取消, 则不删除
  ElMessageBox.confirm('此操作将永久删除该部门, 是否继续?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {//点击确定, 执行删除
    const result = await deleteDeptApi(id);
    if(result.code){
      ElMessage.success('删除成功');
      search();
    }
  }).catch(() => {//点击取消
    ElMessage.info('已取消删除')
  });
}

// --- 新增部门的表单 
// 表单数据
const dept = ref({name: ''});

// 表单引用
const addFormRef = ref(null);
// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ]
};

// 对话框状态 - 显示与隐藏
const addDialogVisible = ref(false);
const formTitle = ref('');

// 显示新增部门对话框
const showAddDialog = () => {
  addDialogVisible.value = true; 
  formTitle.value = '新增部门';
  dept.value = {name: ''}

  //重置表单校验数据 
  if(addFormRef.value){ //表单存在
    addFormRef.value.resetFields();
  }
};


//保存部门 - 新增 / 修改
const save = async () => {
  //表单校验通过再保存部门数据
  addFormRef.value.validate(async valid => { // valid: 表单校验是否通过 , true: 通过; false : 不通过
    if(valid){
      let result; 
      if(dept.value.id){ //修改
        result = await updateDeptApi(dept.value);
      }else {
        result = await addDeptApi(dept.value);
      }
      
      if(result.code){ //成功
        addDialogVisible.value = false;
        search(); 
        //提示保存成功的信息
        ElMessage.success('保存成功');
      }else {
        ElMessage.error(result.msg);
      }
    }
  });
}
</script>

<template>
  <h1>部门管理</h1>
  <br>
  <!-- 定义一个按钮, 按钮名字: +新增部门 -->
  <el-button type="primary" @click="showAddDialog">+ 新增部门</el-button>
  <br><br>

  <!-- 表格 -->
  <el-table :data="deptList" style="width: 100%" border>
    <el-table-column type="index" label="序号" width="80" align="center"/>
    <el-table-column prop="name" label="部门名称" width="200" align="center"/>
    <el-table-column prop="updateTime" label="最后修改时间" width="250" align="center"/>
    <el-table-column fixed="right" label="操作" align="center">
      <template #default="{ row }">
        <el-button type="primary" size="small" @click="handleEdit(row.id)">编辑</el-button>
        <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- 新增部门的对话框 -->
  <el-dialog v-model="addDialogVisible" :title="formTitle" style="width: 500px;">
    <el-form :model="dept" :rules="rules" ref="addFormRef">
      <el-form-item prop="name" label="部门名称">
        <el-input v-model="dept.name" placeholder="请输入部门名称，长度为2-10位"></el-input>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="addDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </span>
    </template>
  </el-dialog>

</template>

<style scoped>

</style>
