
<template>
  <div class="app-container">
    <div class="search-container">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item>
           <el-select  v-model="queryParams.assetCode"  placeholder="资产类型"  size="large"  style="width: 240px">
            <el-option  v-for="item in assetTypeList"  :key="item.value"  :label="item.label"  :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.sceneCode" placeholder="业务场景" clearable size="large" style="width: 180px">
            <el-option v-for="item in sceneList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.reqNo" placeholder="业务单号" clearable size="large" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button type="primary" @click="handleAdd(1)">新增资产</el-button>
          <el-button type="primary" @click="handleAdd(2)">扣减资产</el-button>
        </el-form-item>
      </el-form>
       <el-row class="mb30">
        <el-col :span="6" v-for="(item,index) in assetList" :key="index">

         <div class="assetItem">{{item.assetCodelable}}: {{ item.availableNum }}</div>
        </el-col>
      
       </el-row>
    </div>

    <el-table
      v-loading="loading"
      :data="memberList"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column label="ID" prop="id" width="100" align="center" />
      <el-table-column prop="sceneDescription" label="业务场景" />
      <el-table-column prop="num" label="资产数量" />
      <el-table-column prop="createTime" label="时间" />
      <el-table-column prop="reqNo" label="业务单号" />
      <el-table-column prop="remark" label="备注" />

      
     
    </el-table>

    <!-- 分页工具条 -->
    <pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="handleQuery"
    />

    <!-- 设置爆单品 -->
    <el-dialog v-model="dialogVisible" :title="assetInfo.type==1?'新增':'扣减'" width="1000px">
      <el-form ref="assetRef" :rules="assetRules" :model="assetInfo" label-width="120px">
        <el-form-item label="资产类型:" prop="assetCode">
            <el-radio-group v-model="assetInfo.assetCode">
              <el-radio :value="item.value" v-for="item in assetTypeList" :key="item.value" >{{item.label}}</el-radio>
            </el-radio-group>
        </el-form-item>
        <el-form-item :label="assetInfo.type==1?'新增资产':'扣减资产'" prop="num">
            <el-input  v-model="assetInfo.num"  placeholder="请输入"  clearable/>
        </el-form-item>
        <el-form-item>
          <el-button   @click="dialogVisible=false">取消</el-button>
          <el-button  type="primary" @click="saveConfig(assetRef)">确定</el-button>
        
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
defineOptions({
  name: "assets",
  inheritAttrs: false,
});
import { getAssetFlow,addAssetFlow,subAssetFlow,getAssetList } from "@/api/ums/assets";
import { useRouter,useRoute } from "vue-router";
const router = useRouter()
const route = useRoute()
const state = reactive({
  // 遮罩层
  loading: true,
  // 选中数组
  total: 0,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    assetCode:'POINT',
    sceneCode: '',
    reqNo: '',

  },
  memberList: [],
});

const { loading, queryParams, memberList, total } = toRefs(state);


const assetTypeList = [
  {
    label: "积分",
    value: "POINT",
  }
]
// GAIN("获得", "GAIN"),
//     USE( "使用", "USE"),
//     LOCK( "锁定", "LOCK"),
//     UNLOCK("解锁", "UNLOCK"),
//     TRANSFER_IN("转账收入", "TRANSFER_IN"),
//     TRANSFER_OUT("转账支出", "TRANSFER_OUT");
const sceneList = [
  { label: "订单下单扣减", value: "ORDER_CREATE_DEDUCT" },
  { label: "订单取消退回", value: "ORDER_CANCEL_REFUND" },
  { label: "订单退款退回", value: "ORDER_REFUND_REFUND" },
  { label: "收益发放增加", value: "PROFIT_GRANT" },
  { label: "转账支出", value: "TRANSFER_OUT" },
  { label: "转账收入", value: "TRANSFER_IN" },
  { label: "系统增加", value: "ADJUST_IN" },
  { label: "系统扣减", value: "ADJUST_OUT" },
];
const typeCodeList= {
  GAIN: "获得",
  USE: "使用",
  LOCK: "锁定",
  UNLOCK: "解锁",
  TRANSFER_IN: "转账收入",
  TRANSFER_OUT: "转账支出",
};
function handleQuery() {
  state.loading = true;
  let params={
    ...state.queryParams,
    memberId:route.query.userId
  }
  getAssetFlow(params).then(({ data }) => {
    state.memberList = data.list;
    state.total = data.total;
    state.loading = false;
  });
}

function resetQuery() {
  state.queryParams = {
    pageNum: 1,
    pageSize: 10,
    assetCode: 'POINT',
    sceneCode: '',
    reqNo: '',
  };
  handleQuery();
}


const dialogVisible =ref(false)

const assetInfo = ref({
  memberId: route.query.userId,
  type:'',
  assetCode:'',
  num:'',

})
const assetRef = ref(null);
const assetRules = {
  assetCode: [{ required: true, message: "请选择资产类型", trigger: "change" }],
  num: [{ required: true, message: "请输入资产数量", trigger: "blur" }],
};

const handleAdd=(type)=>{
  assetInfo.value.type = type;
  assetInfo.value.assetCode = '';
  assetInfo.value.num = '';
  dialogVisible.value = true;
}
const saveConfig =async (formEl: FormInstance | undefined) =>  {
   if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      if(assetInfo.value.type=='1'){
        addAsset()
      }else{
        deductAsset()
      }
      
    } else {
      console.log('error submit!', fields)
    }
  })
};
const addAsset = (type) => {
  let params = {
    ...assetInfo.value,
    num:assetInfo.value.num
  }
  addAssetFlow(params).then(res => { 
    if (res.code === '00000') {
      dialogVisible.value = false;
      handleQuery();
      getAssetListFunc()
      assetInfo.value = {
        memberId: route.query.userId,
        type:'',
        assetCode:'',
        num:'',
      }
    }
  });
};
const deductAsset = (type) => {
  // 扣减资产逻辑
    let params = {
    ...assetInfo.value,
    num:assetInfo.value.num
  }
    subAssetFlow(params).then(res => { 
    if (res.code === '00000') {
      dialogVisible.value = false;
      handleQuery();
        getAssetListFunc()
      assetInfo.value = {
        memberId: route.query.userId,
        type:'',
        assetCode:'',
        num:'',
      }
    }
  });
};
const assetList = ref([]);
const getAssetListFunc=async()=>{
  let res = await getAssetList({memberId:route.query.userId});
  if(res.code=='00000'){
    let keys={
      POINT: "积分",
    }
    res.data.forEach(item => {
      item.assetCodelable = keys[item.assetCode];
    });
    assetList.value = res.data;
  }

}
onMounted(() => {
  handleQuery();
  getAssetListFunc()
});
</script>
<style lang="scss" scoped>
.mb30{
  margin-bottom: 30px;
}
</style>


