<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { supplierApi } from '@/api/supplier'
import { blacklistApi } from '@/api/blacklist'

const router = useRouter()
const submitting = ref(false)
const blacklistChecking = ref(false)
const creditCodeValid = ref<boolean | null>(null)
const creditCodeMsg = ref('')

// 统一社会信用代码格式校验（18位：数字+大写字母）
const validateCreditCode = (code: string): boolean => {
  if (!code) return true // 选填
  return /^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}$/.test(code)
}

const checkBlacklist = async (creditCode: string) => {
  if (!creditCode || !validateCreditCode(creditCode)) return
  blacklistChecking.value = true
  try {
    const isBlacklisted = await blacklistApi.checkByCreditCode(creditCode)
    if (isBlacklisted) {
      creditCodeValid.value = false
      creditCodeMsg.value = '该统一社会信用代码已被列入黑名单，无法注册'
    } else {
      creditCodeValid.value = true
      creditCodeMsg.value = '信用代码校验通过'
    }
  } catch {
    // 校验接口不可用时降级，不阻止提交
    creditCodeValid.value = null
    creditCodeMsg.value = ''
  } finally {
    blacklistChecking.value = false
  }
}

const form = reactive({
  supplierName: '',
  creditCode: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  legalPerson: '',
  address: '',
  supplierType: 1,
  remark: '',
})

const formRules = {
  supplierName: [{ required: true, message: '请输入企业全称', trigger: 'blur' }],
  creditCode: [
    {
      validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
        if (value && !validateCreditCode(value)) {
          callback(new Error('统一社会信用代码格式不正确（18位数字+大写字母）'))
        } else if (creditCodeValid.value === false) {
          callback(new Error('该信用代码已被列入黑名单'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
        if (value && !/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('手机号格式不正确'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  contactEmail: [{
    validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
      if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error('邮箱格式不正确'))
      } else {
        callback()
      }
    },
    trigger: 'blur',
  }],
}

const formRef = ref()

const generateCode = () => {
  const rand = Math.floor(Math.random() * 90000 + 10000)
  return `SUP${rand}`
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await supplierApi.create({
      supplierCode: generateCode(),
      ...form,
    })
    ElMessage.success('注册申请已提交，请等待采购方审核。审核通过后将收到账号信息。')
    router.push('/login')
  } catch { /* 拦截器处理 */ } finally {
    submitting.value = false
  }
}

const handleCreditCodeBlur = () => {
  creditCodeValid.value = null
  creditCodeMsg.value = ''
  if (form.creditCode) {
    if (!validateCreditCode(form.creditCode)) {
      creditCodeValid.value = false
      creditCodeMsg.value = '统一社会信用代码格式不正确（18位数字+大写字母）'
    } else {
      checkBlacklist(form.creditCode)
    }
  }
}
</script>

<template>
  <div class="register-shell">
    <div class="register-card">
      <div class="register-header">
        <h1 class="register-title">供应商自助注册</h1>
        <p class="register-subtitle">填写企业信息，提交准入申请，审核通过后将获得账号</p>
      </div>

      <el-steps :active="0" finish-status="success" align-center class="register-steps">
        <el-step title="填写资料" description="企业基本信息" />
        <el-step title="资质审核" description="初审 → 复审 → 终审" />
        <el-step title="账号开通" description="获取门户账号" />
      </el-steps>

      <el-divider />

      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px" label-position="right" class="register-form">
        <div class="form-section-title">企业信息</div>
        <el-form-item label="企业全称" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="请与营业执照上的名称完全一致" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="统一信用代码" prop="creditCode">
          <el-input
            v-model="form.creditCode"
            placeholder="18位统一社会信用代码（选填）"
            maxlength="18"
            show-word-limit
            :class="{ 'is-valid': creditCodeValid === true, 'is-invalid': creditCodeValid === false }"
            @blur="handleCreditCodeBlur"
          >
            <template #suffix>
              <el-icon v-if="blacklistChecking" class="is-loading"><Loading /></el-icon>
              <el-icon v-else-if="creditCodeValid === true" style="color: #67c23a"><CircleCheck /></el-icon>
              <el-icon v-else-if="creditCodeValid === false" style="color: #f56c6c"><CircleClose /></el-icon>
            </template>
          </el-input>
          <div v-if="creditCodeMsg" :class="creditCodeValid === false ? 'credit-invalid' : 'credit-valid'">{{ creditCodeMsg }}</div>
          <div v-else class="form-tip">须与营业执照一致，审核时将自动校验黑名单</div>
        </el-form-item>
        <el-form-item label="法定代表人">
          <el-input v-model="form.legalPerson" placeholder="选填" maxlength="50" />
        </el-form-item>

        <div class="form-section-title">联系人信息</div>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="contactEmail">
          <el-input v-model="form.contactEmail" placeholder="选填" maxlength="100" />
        </el-form-item>
        <el-form-item label="企业地址">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="选填" maxlength="200" show-word-limit />
        </el-form-item>

        <div class="form-section-title">补充说明</div>
        <el-form-item label="供应品类">
          <el-select v-model="form.supplierType" placeholder="请选择" style="width:100%">
            <el-option label="原材料" :value="1" />
            <el-option label="零部件" :value="2" />
            <el-option label="辅料" :value="3" />
            <el-option label="设备" :value="4" />
            <el-option label="服务" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="其他需要说明的信息（选填）" maxlength="500" show-word-limit />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit" style="width:100%">
            提交注册申请
          </el-button>
        </el-form-item>
        <div class="register-footer">
          已有账号？<el-link type="primary" @click="router.push('/login')">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.register-shell {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5f9 0%, #e8f4f0 50%, #f5f0e8 100%);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40px 16px 60px;
}
.register-card {
  width: 100%;
  max-width: 680px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
  padding: 40px 48px;
}
.register-header {
  text-align: center;
  margin-bottom: 28px;
}
.register-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a2b3c;
  margin: 0 0 8px;
}
.register-subtitle {
  font-size: 14px;
  color: #8a98aa;
  margin: 0;
}
.register-steps {
  margin-bottom: 8px;
}
.register-form {
  margin-top: 20px;
}
.form-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e4ebf3;
}
.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
.credit-valid {
  margin-top: 4px;
  font-size: 12px;
  color: #67c23a;
}
.credit-invalid {
  margin-top: 4px;
  font-size: 12px;
  color: #f56c6c;
}
.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
</style>