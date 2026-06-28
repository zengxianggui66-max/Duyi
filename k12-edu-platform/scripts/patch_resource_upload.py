from pathlib import Path

p = Path(__file__).resolve().parents[1] / 'src/views/resource/ResourceUpload.vue'
text = p.read_text(encoding='utf-8')
start = text.index('        <!-- Step 2: 文件上传 -->')
end = text.index('        <!-- Step 3: 分类配置 -->', start)
replacement = """        <!-- Step 2: 文件上传 -->
        <motion v-show="currentStep === 1" class="step-panel">
          <UploadStepFile ref="uploadStepFileRef" />
        </div>

"""
replacement = replacement.replace('<motion', '<motion').replace('</motion>', '</motion>')
replacement = """        <!-- Step 2: 文件上传 -->
        <div v-show="currentStep === 1" class="step-panel">
          <UploadStepFile ref="uploadStepFileRef" />
        </div>

"""
text = text[:start] + replacement + text[end:]

if 'UploadStepFile' not in text:
    text = text.replace(
        "import { fileApi } from '@/api'",
        "import { fileApi } from '@/api'\nimport UploadStepFile from '@/components/upload/UploadStepFile.vue'",
    )
    text = text.replace(
        'const uploadRef = ref<UploadInstance>()',
        'const uploadRef = ref<UploadInstance>()\nconst uploadStepFileRef = ref<InstanceType<typeof UploadStepFile> | null>(null)',
    )

p.write_text(text, encoding='utf-8')
print('patched upload')
