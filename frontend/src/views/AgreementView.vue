<template>
  <div class="agreement-page">
    <div class="agreement-card">
      <header class="agreement-header">
        <div>
          <h1 class="agreement-title">{{ meta.title }}</h1>
          <p class="agreement-meta">版本：{{ meta.version }} &nbsp;·&nbsp; 生效日期：{{ meta.effectiveDate }}</p>
        </div>
        <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
      </header>

      <main class="agreement-body" v-html="meta.content"></main>

      <footer class="agreement-footer">
        <el-button type="primary" @click="goBack">我已阅读并理解</el-button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 版本号与后端 AGREEMENT_VERSION 常量保持一致
const AGREEMENT_VERSION = 'v1'
const EFFECTIVE_DATE = '2026-09-28'

const agreements = {
  user: {
    title: '用户协议',
    version: AGREEMENT_VERSION,
    effectiveDate: EFFECTIVE_DATE,
    content: `
      <p class="intro">欢迎您使用 OA员工管理系统（以下简称"本系统"）。请您在注册前仔细阅读本协议，一旦您勾选同意并完成注册，即表示您已充分理解并接受本协议的全部内容。</p>

      <h2>一、服务内容</h2>
      <p>1.1 本系统为轻量化企业协作平台，为您提供员工信息管理、部门与职位管理、个人资料维护、智能问答等服务。</p>
      <p>1.2 我们可能会不定期对服务内容进行升级或调整，并以系统公告、页面提示等适当方式向您告知。</p>

      <h2>二、账号注册与管理</h2>
      <p>2.1 您须使用真实、有效的电子邮箱注册账号，并妥善保管邮箱收到的验证码。因验证码泄露造成的损失由您自行承担。</p>
      <p>2.2 您应设置安全性较高的密码，并对账号下的所有操作负责。如发现账号被他人盗用，应立即通知系统管理员。</p>
      <p>2.3 若您提供虚假信息、冒用他人邮箱注册，或利用本系统从事违法违规活动，我们有权暂停或终止向您提供服务。</p>

      <h2>三、用户行为规范</h2>
      <p>3.1 您在使用本系统过程中，不得发布、传播违法违规信息，不得侵害他人合法权益。</p>
      <p>3.2 未经授权，不得对本系统进行反向工程、爬取数据、恶意攻击或干扰系统正常运行。</p>
      <p>3.3 您在系统内录入的工作数据（如个人资料、部门信息等）应遵守所在单位的相关管理制度。</p>

      <h2>四、知识产权</h2>
      <p>4.1 本系统的软件、界面设计、图标等知识产权归系统运营方所有，未经许可不得复制或用于商业用途。</p>
      <p>4.2 您在本系统中产生的个人资料等数据的归属及使用方式，详见《隐私政策》。</p>

      <h2>五、免责声明</h2>
      <p>5.1 因不可抗力、网络故障、系统维护等非我们过错原因导致服务中断的，我们将尽力恢复但不承担由此造成的间接损失。</p>
      <p>5.2 我们会尽力保障服务稳定，但不对服务的不中断、无错误作绝对保证。</p>

      <h2>六、协议的变更</h2>
      <p>6.1 我们可能根据业务发展修订本协议，修订后的协议会通过系统公告等方式公布。若您在协议修订后继续使用本系统，视为接受修订后的内容。</p>
      <p>6.2 重大变更将以页面弹窗等方式提示您重新确认。</p>

      <h2>七、其他</h2>
      <p>7.1 本协议的订立、履行与解释适用中华人民共和国法律。</p>
      <p>7.2 若您对本协议有任何疑问，可通过系统内的联系方式与管理员沟通。</p>
    `,
  },
  privacy: {
    title: '隐私政策',
    version: AGREEMENT_VERSION,
    effectiveDate: EFFECTIVE_DATE,
    content: `
      <p class="intro">我们深知个人信息对您的重要性，将严格依照《中华人民共和国个人信息保护法》等法律法规，保护您的个人信息。请您仔细阅读本政策，了解我们如何收集、使用、存储和保护您的信息。</p>

      <h2>一、我们收集的信息</h2>
      <p>1.1 账号信息：注册邮箱、登录密码（加密存储）、账号角色与状态。</p>
      <p>1.2 个人资料：姓名、性别、手机号、头像、所属部门与职位、入职时间等由您或管理员录入的信息。</p>
      <p>1.3 使用信息：登录与操作产生的必要日志（如登录 IP、操作时间），用于安全审计。</p>
      <p>1.4 安全验证信息：滑块验证结果与协议签署记录（含时间与 IP），用于确认操作由您本人发起。</p>

      <h2>二、信息的使用</h2>
      <p>2.1 为您提供账号注册登录、资料管理、组织人事管理等核心功能。</p>
      <p>2.2 保障账号与系统安全：防范暴力破解、恶意注册等风险行为。</p>
      <p>2.3 您在"AI 问答"中提交的问题仅用于生成回答，未经您授权不会用于其他用途。</p>

      <h2>三、信息的存储与保护</h2>
      <p>3.1 您的个人信息存储于本系统的数据库中，密码采用不可逆加密算法存储，任何人（包括管理员）均无法查看明文。</p>
      <p>3.2 我们采取传输加密、访问控制、操作审计等技术与管理措施防止信息泄露、篡改或丢失。</p>
      <p>3.3 您的协议签署记录、验证记录等安全数据仅保留实现安全目的所必需的期限。</p>

      <h2>四、信息的共享</h2>
      <p>4.1 我们不会向任何第三方出售您的个人信息。</p>
      <p>4.2 仅在法律法规要求或您明确授权的情况下，才可能对外提供必要的信息。</p>

      <h2>五、您的权利</h2>
      <p>5.1 您可以在"个人信息"页面查看、更正自己的资料，或联系管理员处理。</p>
      <p>5.2 如需注销账号或删除个人信息，可联系系统管理员，我们将在核实身份后及时处理。</p>

      <h2>六、未成年人保护</h2>
      <p>6.1 本系统面向企业内部员工使用，不面向未成年人提供服务。</p>

      <h2>七、政策更新</h2>
      <p>7.1 本政策可能适时修订，重大变更将通过页面提示等方式通知您。</p>
      <p>7.2 若您对本政策有任何疑问或建议，可通过系统内的联系方式与管理员沟通。</p>
    `,
  },
}

const meta = computed(() => {
  const type = route.params.type
  return agreements[type] || null
})

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/register')
  }
}
</script>

<script>
export default { name: 'AgreementView' }
</script>

<style scoped>
.agreement-page {
  min-height: 100vh;
  padding: 32px 16px 48px;
  background: #f5f7fa;
  box-sizing: border-box;
}

.agreement-card {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(31, 35, 41, 0.06);
  overflow: hidden;
}

.agreement-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 28px 40px 20px;
  border-bottom: 1px solid #f0f1f3;
}

.agreement-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2329;
}

.agreement-meta {
  margin: 8px 0 0;
  font-size: 13px;
  color: #8f959e;
}

.agreement-body {
  padding: 8px 40px 32px;
  max-height: 60vh;
  overflow-y: auto;
}

.agreement-body :deep(.intro) {
  padding: 12px 16px;
  background: #f2f6ff;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.8;
  color: #4e5969;
}

.agreement-body :deep(h2) {
  margin: 28px 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2329;
}

.agreement-body :deep(p) {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.8;
  color: #4e5969;
}

.agreement-footer {
  padding: 16px 40px 28px;
  text-align: center;
  border-top: 1px solid #f0f1f3;
}
</style>
