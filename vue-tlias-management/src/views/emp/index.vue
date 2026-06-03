<script setup>
import { ref, watch, onMounted } from 'vue';
import { queryPageApi, addEmpApi } from '@/api/emp'
import { queryAllDeptApi } from '@/api/dept'
import { ElMessage } from 'element-plus';

//职位列表数据
const jobs = ref([{ name: '班主任', value: 1 },{ name: '讲师', value: 2 },{ name: '学工主管', value: 3 },{ name: '教研主管', value: 4 },{ name: '咨询师', value: 5 },{ name: '其他', value: 6 }])
//性别列表数据
const genders = ref([{ name: '男', value: 1 }, { name: '女', value: 2 }])
//部门列表数据
const depts = ref([])

//员工列表查询搜索条件封装对象
const searchEmp = ref({
  name: '',
  gender: '',
  date: [],
  begin: '',
  end: ''
});

//侦听-searchEmp中的date属性
watch(() => searchEmp.value.date, (newValue, oldValue) => {
  if(searchEmp.value.date.length == 2){
    searchEmp.value.begin = newValue[0];
    searchEmp.value.end = newValue[1];
  }else {
    searchEmp.value.begin = '';
    searchEmp.value.end = '';
  }
})

//钩子函数
onMounted(() => {
  search()
  queryAllDept()
});

//查询所有部门
const queryAllDept = async () => {
  const result = await queryAllDeptApi()
  if(result.code){
    depts.value = result.data
  }
}

//查询
const search = async () => {
   //发送请求, 查询员工数据
   const result = await queryPageApi(searchEmp.value.name, 
                        searchEmp.value.gender, 
                        searchEmp.value.begin, 
                        searchEmp.value.end, 
                        currentPage.value, 
                        pageSize.value);
  if(result.code){
    empList.value = result.data.rows;
    total.value = result.data.total;
  }
};

// 清空
const clear = () => {
  searchEmp.value = { name: '', gender: '', date: [], begin: '', end: '' };
  search()
};

//列表数据
const empList = ref([])

// 获取职位名称
const getJobTitle = (job) => {
  const jobs = ['其他', '班主任', '讲师', '学工主管', '教研主管', '咨询师']
  return jobs[job] || '其他'
}

// 编辑
const update = (id) => {
  console.log('编辑:', id)
}
//删除
const deleteById = (id) => {
  console.log('删除:', id)
}

// 分页相关状态
const currentPage = ref(1) //当前页码
const pageSize = ref(10) //每页显示记录数
const total = ref(0) //总记录数

// 页码发生变化时触发
const handleCurrentChange = (val) => {
  currentPage.value = val
  search()
}

// 处理每页显示记录数的变化
const handleSizeChange = (val) => {
  pageSize.value = val
  search()
}


// -------------------> 新增员工表单
//新增/修改表单
const employeeFormRef = ref(null)
const employee = ref({
  username: '',
  name: '',
  gender: '',
  phone: '',
  job: '',
  salary: '',
  deptId: '',
  entryDate: '',
  image: '',
  exprList: []
})

// 控制弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')

//新增员工
const add  = () => {
  dialogVisible.value = true
  dialogTitle.value = '新增员工'
  employee.value = {username: '', name: '', gender: '', phone: '', job: '', salary: '', deptId: '', entryDate: '', image: '', exprList: []}
}


// 图片上传成功后触发
const handleAvatarSuccess = (response) => {
  console.log(response);
  if(response.code){
    employee.value.image = response.data;
  }
}

// 文件上传之前触发 --> 返回true: 继续上传 --> 返回false: 不上传
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('只支持上传图片')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('只能上传10M以内图片')
    return false
  }
  return true
}


//员工工作经历 - 新增 / 删除
const addExprItem = () => {
  //往数组 employee.value.exprList 中添加一个元素. (push 添加)
  employee.value.exprList.push({
    company:'',
    job: '',
    begin: '',
    end: '',
    exprDate: []
  })
}

//删除工作经历
const delExprItem = (index) => {
  employee.value.exprList.splice(index, 1) //删除数组中指定索引的元素
}

//watch 侦听
//{"username": "", "name": "", "exprList": [{"company":"", "job":"", "begin":"", "end":"", "exprDate":["2024-08-08","2024-08-24"]}]}
watch(employee, (newVal, oldVal) => {
  const exprList = employee.value.exprList;
  if(exprList && exprList.length > 0){
    //遍历exprList
    exprList.forEach((expr) => {
      if(expr.exprDate && expr.exprDate.length == 2){
        expr.begin = expr.exprDate[0]; //第一个元素
        expr.end = expr.exprDate[1]; //第二个元素
      }else {
        expr.begin = '';
        expr.end = '';
      }
    })
  }
}, { deep: true })


//保存员工信息
const save = async () => {
  const result = await addEmpApi(employee.value);
  if(result.code){
    //关闭对话框
    dialogVisible.value = false;
    //提示成功信息
    ElMessage.success('保存成功');
    //查询员工列表
    search();
  }else{
    ElMessage.error(result.msg);
  }
}

//测试代码 watch - age
// const age = ref(0);
// watch(age, (newValue, oldValue) => {
//   console.log(`变化前: ${oldValue}, 变化后: ${newValue}`);
// });

// watch(searchEmp, (newValue, oldValue) => {
//   console.log( searchEmp.value);
// }, { deep: true, immediate: true }) //深度侦听 , 立即侦听
</script>

<template>
  <h1>员工管理</h1> <br>
  
  <!-- 搜索栏 -->
  <div class="search-bar">
    <el-form :model="searchEmp" inline label-width="80px">
      <!-- 姓名 -->
      <el-form-item label="姓名">
        <el-input v-model="searchEmp.name" placeholder="请输入员工姓名"></el-input>
      </el-form-item>

      <!-- 性别 -->
      <el-form-item label="性别">
        <el-select v-model="searchEmp.gender" placeholder="请选择">
          <el-option label="男" value="1"></el-option>
          <el-option label="女" value="2"></el-option>
        </el-select>
      </el-form-item>

      <!-- 入职日期 -->
      <el-form-item label="入职日期">
        <el-date-picker
          v-model="searchEmp.date"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        ></el-date-picker>
      </el-form-item>

      <!-- 按钮 -->
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="clear">清空</el-button>
      </el-form-item>
    </el-form>
  </div>

  <!-- 按钮 -->
  <el-button type="primary" @click="add">+ 新增员工</el-button>
  <el-button type="danger">- 批量删除</el-button>
  <br><br>

  <!-- 表格 -->
  <el-table :data="empList" border stripe style="width: 100%">
    <el-table-column type="selection" width="55"  align="center"/>
    <el-table-column prop="name" label="姓名" width="120"  align="center"/>
    <el-table-column label="性别" width="150"  align="center">
      <template #default="{ row }">
        {{ row.gender === 1 ? '男' : '女' }}
      </template>
    </el-table-column>
    <el-table-column prop="image" label="头像"  width="150"  align="center">
      <template #default="{ row }">
        <img :src="row.image" class="avatar" />
      </template>
    </el-table-column>
    <el-table-column prop="deptName" label="部门名称"  width="150" align="center"/>
    <el-table-column prop="job" label="职位"  width="150" align="center">
      <template #default="{ row }">
        {{ getJobTitle(row.job) }}
      </template>
    </el-table-column>
    <el-table-column prop="entryDate" label="入职日期"  width="200" align="center"/>
    <el-table-column prop="updateTime" label="最后操作时间"  width="200" align="center"/>
    <el-table-column fixed="right" label="操作" align="center">
      <template #default="{ row }">
        <el-button size="small" type="primary" @click="update(row.id)">编辑</el-button>
        <el-button size="small" type="danger" @click="deleteById(row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <br>

  <!-- 分页条 -->
  <el-pagination
    background
    layout="total, sizes, prev, pager, next, jumper"
    :page-sizes="[5, 10, 15, 20, 30, 50, 100]"
    :page-size="pageSize"
    :total="total"
    :current-page="currentPage"
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
  />


  <!-- 新增/修改员工的对话框 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form ref="employeeFormRef" :model="employee" label-width="80px">
        <!-- 基本信息 -->
        <!-- 第一行 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="employee.username" placeholder="请输入员工用户名，2-20个字"></el-input>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="employee.name" placeholder="请输入员工姓名，2-10个字"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二行 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="employee.gender" placeholder="请选择性别" style="width: 100%;">
                <el-option v-for="gender in genders" :label="gender.name" :value="gender.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="employee.phone" placeholder="请输入员工手机号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位">
              <el-select v-model="employee.job" placeholder="请选择职位" style="width: 100%;">
                <el-option v-for="job in jobs" :label="job.name" :value="job.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资">
              <el-input v-model="employee.salary" placeholder="请输入员工薪资"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第四行 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-select v-model="employee.deptId" placeholder="请选择部门" style="width: 100%;">
                <el-option v-for="dept in depts" :label="dept.name" :value="dept.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="employee.entryDate" type="date" style="width: 100%;" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD"></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第五行 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                action="/api/upload"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                >
                <img v-if="employee.image" :src="employee.image" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
        

        <!-- 工作经历 -->
        <!-- 第六行 -->
        <el-row :gutter="10">
          <el-col :span="24">
            <el-form-item label="工作经历">
              <el-button type="success" size="small" @click="addExprItem">+ 添加工作经历</el-button>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 第七行 ...  工作经历 -->
        <el-row :gutter="3" v-for="(expr, index) in employee.exprList" >
          <el-col :span="10">
            <el-form-item size="small" label="时间" label-width="80px">
              <el-date-picker type="daterange" v-model="expr.exprDate" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" ></el-date-picker>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item size="small" label="公司" label-width="60px">
              <el-input placeholder="请输入公司名称" v-model="expr.company"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item size="small" label="职位" label-width="60px">
              <el-input placeholder="请输入职位" v-model="expr.job"></el-input>
            </el-form-item>
          </el-col>

          <el-col :span="2">
            <el-form-item size="small" label-width="0px">
              <el-button type="danger" @click="delExprItem(index)">- 删除</el-button>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>
      
      <!-- 底部按钮 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </span>
      </template>
  </el-dialog>

</template>

<style scoped>
.avatar {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 20%;
}

.avatar-uploader .avatar {
  width: 78px;
  height: 78px;
  display: block;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 78px;
  height: 78px;
  text-align: center;
  /* 添加灰色的虚线边框 */
  border: 1px dashed var(--el-border-color);
}
</style>