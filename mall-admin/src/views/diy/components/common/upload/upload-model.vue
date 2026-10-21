<!-- 上传组件 -->
<template>
    <el-dialog v-model="dialogVisible" class="radius-lg" width="1168" draggable append-to-body :close-on-click-modal="false" @close="close_dialog">
        <template #header>
            <div class="title center re">
                <div class="tc size-16 fw-b">{{ upload_type_name }}上传</div>
            </div>
        </template>
        <div class="upload-content pa-20" @paste="handle_paste">
            <el-form ref="ruleFormRef" :model="form" :rules="rules" label-width="85" status-icon>
                <el-form-item label="上传方式">
                    <el-radio-group v-model="form.type" @change="upload_type_change">
                        <el-radio value="loc">本地上传</el-radio>
                        <!-- 隐藏扫码上传和网络上传选项 -->
                        <!-- <el-radio value="scan">扫码上传</el-radio>
                        <el-radio value="web">网络上传</el-radio> -->
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="上传至分组" prop="category_id">
                    <div class="form-item-width">
                        <el-cascader v-model="form.category_id" class="w" :options="cascader_data" placeholder="请选择" :show-all-levels="false" filterable clearable @change="category_id_change"></el-cascader>
                    </div>
                </el-form-item>
                <template v-if="form.type == 'loc'">
                    <div class="flex-row jc-sb align-c mt-30">
                        <div class="flex-row">
                            <el-upload ref="fileUpload1" v-model:file-list="file_list" multiple action="#" :accept="exts_text" :auto-upload="false" :show-file-list="false" :on-change="upload_change" :before-upload="before_upload" :limit="limit" :on-exceed="handle_exceed">
                                <template #trigger>
                                    <el-button @click="folder_mode(false)"> 上传{{ upload_type_name }} </el-button>
                                    <!-- 隐藏上传文件夹按钮 -->
                                    <!-- <el-button @click="folder_mode(true)"> 上传文件夹 </el-button> -->
                                </template>
                            </el-upload>
                        </div>
                        <div class="flex-row align-c gap-10 size-12">
                            <div class="cr-success">上传成功:{{ form.file.filter((obj) => obj.status == 'success').length }}</div>
                            <div class="cr-error">上传失败:{{ form.file.filter((obj) => obj.status == 'error').length }}</div>
                            <div>等待上传:{{ form.file.filter((obj) => obj.status == 'ready').length }}</div>
                            <el-button @click="clear_list_event">清空列表</el-button>
                        </div>
                    </div>
                    <div class="table mt-10">
                        <div class="table-header">
                            <div class="table-row">
                                <div class="table-cell">文件名</div>
                                <div class="table-cell">文件大小</div>
                                <div class="table-cell">上传状态</div>
                                <div class="table-cell-operate">操作</div>
                            </div>
                        </div>
                        <div id="dropzone" @dragover.prevent="handle_drag_in" @dragenter="handle_drag_in" @dragleave="handle_drag_leave" @drop.prevent="handle_drop">
                            <el-scrollbar v-if="!is_dragging && form.file.length > 0" height="341px">
                                <div class="table-body">
                                    <div v-for="(item, index) in form.file" :key="item.file.name + item.file.size" class="re">
                                        <div class="progress" :style="'width:' + item.progress + '%'"></div>
                                        <div class="table-row" :class="item.status == '上传失败' ? 'bg-table-rwo-error' : ''">
                                            <div class="table-cell">
                                                <template v-if="type == 'video'">
                                                    <div class="preview-img radius-sm">
                                                        <icon name="video" size="12" color="9"></icon>
                                                    </div>
                                                </template>
                                                <template v-else-if="type == 'file'">
                                                    <div class="preview-img radius-sm">
                                                        <div class="bg-f5 img flex-row jc-c align-c radius h w">
                                                            <icon :name="getFileIcon(item.file.name)" size="12" color="9"></icon>
                                                        </div>
                                                    </div>
                                                </template>
                                                <template v-else>
                                                    <el-image :src="file_to_base64(item.file)" class="preview-img radius-sm" fit="contain">
                                                        <template #error>
                                                            <div class="bg-f5 img flex-row jc-c align-c radius h w">
                                                                <icon name="error-img" size="12"></icon>
                                                            </div>
                                                        </template>
                                                    </el-image>
                                                </template>
                                                <div class="desc">{{ item.file.name }}</div>
                                            </div>
                                            <div class="table-cell">{{ annex_size_to_unit(item.file.size) }}</div>
                                            <div class="table-cell" :class="item.status">{{ item.status == 'loading' ? '上传中' : item.status == 'success' ? '上传成功' : item.status == 'error' ? '上传失败' : '等待上传' }}{{ item.status == 'loading' ? '(' + item.progress + '%)' : '' }}</div>
                                            <div class="table-cell-operate" @click="del_upload(index)">移除</div>
                                        </div>
                                    </div>
                                </div>
                            </el-scrollbar>
                            <div v-show="is_dragging || form.file.length < 1" class="folder-upload mt-20" :class="is_dragging ? 'active' : ''">
                                <el-upload ref="fileUpload2" v-model:file-list="file_list" class="upload-2" :accept="exts_text" multiple action="#" :auto-upload="false" :show-file-list="false" :on-change="upload_change" :before-upload="before_upload" :limit="limit" :on-exceed="handle_exceed">
                                    <div class="flex-col jc-c align-c">
                                        <icon name="add" size="60" color="#dbeef6"></icon>
                                        <p class="size-18 cr-c fw-b">请将需要上传的文件/文件夹拖到此处或粘贴</p>
                                    </div>
                                </el-upload>
                            </div>
                        </div>
                    </div>
                </template>
                <template v-else-if="form.type == 'scan'">
                    <el-form-item label="二维码" class="mb-10">
                        <qrcode :src="form.qrcode" :is-mask="is_mask"></qrcode>
                    </el-form-item>
                    <div class="table">
                        <div class="table-header">
                            <div class="table-row">
                                <div class="table-cell">文件名</div>
                                <div class="table-cell">文件大小</div>
                                <div class="table-cell-operate">操作</div>
                            </div>
                        </div>
                        <el-scrollbar height="224px">
                            <div v-if="scan_file_list.length > 0">
                                <div class="table-body">
                                    <div v-for="(item, index) in scan_file_list" :key="index" class="table-row">
                                        <div class="table-cell">
                                            <template v-if="type == 'video'">
                                                <div class="preview-img radius-sm">
                                                    <icon name="video" size="12" color="9"></icon>
                                                </div>
                                            </template>
                                            <template v-else-if="type == 'file'">
                                                <div class="preview-img radius-sm">
                                                    <div class="bg-f5 img flex-row jc-c align-c radius h w">
                                                        <icon :name="getFileIconByExt(item.ext)" size="12" color="9"></icon>
                                                    </div>
                                                </div>
                                            </template>
                                            <template v-else>
                                                <el-image :src="item.url" class="preview-img radius-sm" fit="contain">
                                                    <template #error>
                                                        <div class="bg-f5 img flex-row jc-c align-c radius h w">
                                                            <icon name="error-img" size="12"></icon>
                                                        </div>
                                                    </template>
                                                </el-image>
                                            </template>
                                            <div class="desc">{{ item.title }}</div>
                                        </div>
                                        <div class="table-cell">{{ annex_size_to_unit(item.size) }}</div>
                                        <div class="table-cell-operate" @click="del_already_upload(item.id, index)">删除</div>
                                    </div>
                                </div>
                            </div>
                            <div v-else>
                                <el-empty description="暂无数据" :image-size="100"></el-empty>
                            </div>
                        </el-scrollbar>
                    </div>
                </template>
                <template v-else-if="form.type == 'web'">
                    <el-form-item label="网络图片">
                        <div class="flex-row align-c gap-10">
                            <el-input v-model="form.web_image" class="form-item-width" placeholder="请输入网络图片地址" clearable />
                            <div class="c-pointer cr-primary size-12" @click="extract_images(ruleFormRef)">提取图片</div>
                        </div>
                    </el-form-item>
                </template>
            </el-form>
        </div>
        <template #footer>
            <span class="dialog-footer">
                <el-button class="plr-28 ptb-10" @click="close_dialog">取消</el-button>
                <!-- 由于隐藏了扫码和网络上传，只保留本地上传按钮 -->
                <el-button v-if="form.type == 'loc'" class="plr-28 ptb-10" type="primary" @click="submit_event(ruleFormRef)">上传</el-button>
                <!-- 以下按钮在隐藏扫码和网络上传后不再需要，但保留以备后续启用
                <el-button v-else-if="form.type == 'scan'" class="plr-28 ptb-10" type="primary" @click="close_dialog">返回图库</el-button>
                <el-button v-else-if="form.type == 'web'" class="plr-28 ptb-10" type="primary" @click="close_all_dialog">确认</el-button>
                -->
            </span>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { UploadAPI, type UploadTreeNode, uploadFileWithProgress } from '@/api/file';
import { uploadStore } from '../../../store';
const upload_store = uploadStore();
import type { UploadFile, UploadFiles, UploadUserFile, FormRules, FormInstance } from 'element-plus';
import { annex_size_to_unit, ext_name, get_math } from '../../../utils';
import { ext_img_name_list, ext_video_name_list, ext_file_name_list, ext_file_name_list_map } from './index';
import Qrcode from '../../base/qrcode/index.vue';
const app = getCurrentInstance();

const props = defineProps({
    type: {
        type: String,
        default: 'img',
    },
    limit: {
        type: Number,
        default: 10000,
    },
    exts: {
        type: Array,
        default: () => ['.png', '.jpg', '.jpeg', '.bmp', '.webp', '.gif'],
    },
    fileSize: {
        type: Number,
        default: 1024 * 1024 * 1024,
    },
});

const dialogVisible = defineModel({ type: Boolean, default: false });

const upload_type_name = computed(() => {
    return props.type === 'img' ? '图片' : props.type === 'video' ? '视频' : '文件';
});

const exts_text = computed(() => props.exts.join(','));
const file_list = ref<UploadUserFile[]>([]);

interface fileData {
    file: File;
    status: string;
    progress: number;
}

interface formData {
    type: string;
    category_id: string[];
    file: fileData[];
    qrcode: string;
    web_image: string;
}

const ruleFormRef = ref<FormInstance>();
const form = ref<formData>({
    type: 'loc',
    category_id: [],
    file: [],
    qrcode: '',
    web_image: '',
});

const rules = reactive<FormRules>({
    category_id: [{ required: true, trigger: 'change', message: '请选择分组' }],
});

// 根据文件扩展名获取图标名称
const getFileIcon = (filename: string) => {
    const ext = '.' + filename.split('.').pop()?.toLowerCase();
    const item = ext_file_name_list_map.find((e) => e.type === ext);
    return item ? item.icon : 'file';
};

// 根据扩展名获取图标（用于扫码上传）
const getFileIconByExt = (ext: string) => {
    const item = ext_file_name_list_map.find((e) => e.type === ext);
    return item ? item.icon : 'file';
};

// 使用计算属性获取级联数据
const cascader_data = computed(() => {
    return upload_store.category.map((tree: UploadTreeNode) => ({
        value: tree.id,
        label: tree.name,
        children: tree.items?.map((item: UploadTreeNode) => ({
            value: item.id,
            label: item.name,
        })),
    }));
});

// 是否给二维码加模糊效果
const is_mask = ref(true);
const timer = ref<any>(null);
const scan_uuid = ref('');

// 上传方式切换
const upload_type_change = (type: any) => {
    // 清除之前的定时器（如果存在）
    if (timer.value && type != 'scan') {
        clearTimeout(timer.value);
        timer.value = null;
    }
    if (type == 'scan') {
        timer.value = setInterval(async () => {
            if (scan_uuid.value.length > 0) {
                const { data } = await UploadAPI.uploadQrcode({ key: scan_uuid.value });
                scan_file_list.value = data || [];
            }
        }, 3000);
        // 如果已经选择了分组，则重新设置二维码
        if (form.value.category_id.length > 0) {
            category_id_change(form.value.category_id);
        }
    }
};

// 选择分组
const category_id_change = (val: any) => {
    if (val && val.length > 0) {
        scan_file_list.value = [];
        is_mask.value = false;
        scan_uuid.value = get_math() + '';
        let new_url = '';
        if (import.meta.env.VITE_APP_BASE_API == '/dev-admin') {
            new_url = get_before_string(import.meta.env.VITE_APP_BASE_API_URL);
        } else {
            new_url = window.location.origin + '/';
        }
        form.value.qrcode = new_url + '?s=ueditor/scanupload/key/' + scan_uuid.value + '/cid/' + val[val.length - 1] + '/type/upload' + (props.type == 'file' ? 'file' : props.type == 'video' ? 'video' : 'image');
    }
};

// 获取字符串中'.'后所有字符
const get_after_string = (str: string) => {
    const index = str.lastIndexOf('.');
    return str.substring(index, str.length);
};

// 获取字符串中'/'前面所有字符
const get_before_string = (str: string) => {
    const index = str.lastIndexOf('admin.php');
    return str.substring(0, index);
};

// ============ 本地上传 ============

const state = reactive({
    uploadEle: null as HTMLInputElement | null,
    uploadList: [],
});

const folder_mode = (type: boolean) => {
    if (!state.uploadEle) {
        state.uploadEle = document.querySelector('.el-upload__input') as HTMLInputElement;
    }
    nextTick(() => {
        (state.uploadEle as HTMLInputElement).webkitdirectory = type;
    });
};

const upload_change = async (uploadFile: UploadFile, uploadFiles: UploadFiles) => {
    const results = uploadFiles.flat(Infinity).filter((f: any) => validExt(f.name) && validSize(f.size));
    const new_upload_files = results.filter((item: UploadFile) => {
        return !form.value.file.find((item2: fileData) => {
            return item2.file.name === item.name && item2.file.size === item.size;
        });
    });
    new_upload_files.forEach((item: UploadFile) => {
        form.value.file.push({
            status: 'ready',
            progress: 0,
            file: item.raw as File,
        });
    });
};

const validExt = (name: string) => props.exts.includes(ext_name(name));
const validSize = (size: number) => size <= props.fileSize;

const before_upload = (file: any) => {
    if (validExt(file.name) && validSize(file.size)) {
        return true;
    }
    return false;
};

const handle_exceed = (files: any, fileList: any) => {
    ElMessageBox.alert(`最多上传 ${props.limit} 个文件!`);
};

const clear_list_event = () => {
    form.value.file = [];
    file_list.value = [];
};

const is_dragging = ref(false);
const dropzone = ref<HTMLElement | null>(null);

onMounted(() => {
    dropzone.value = document.getElementById('dropzone') as HTMLElement;
});

const handle_drag_in = (e: DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    is_dragging.value = true;
};

const handle_drag_leave = (event: DragEvent) => {
    event.preventDefault();
    event.stopPropagation();
    const leaveTimer = setTimeout(() => {
        if (!document.getElementById('dropzone')?.contains(event.relatedTarget as Node)) {
            is_dragging.value = false;
        }
    }, 50);
    event.currentTarget?.addEventListener(
        'dragenter',
        () => {
            clearTimeout(leaveTimer);
        },
        { once: true }
    );
};

const handle_drop = async (event: any) => {
    event.preventDefault();
    event.stopPropagation();
    is_dragging.value = false;
    let results = await Promise.all([...event.dataTransfer.items].map((item) => handle_entry(item.webkitGetAsEntry())));
    results = results.flat(Infinity).filter((f: any) => validExt(f.name) && validSize(f.size));
    if (results.length + form.value.file.length <= props.limit) {
        results.forEach((file: any) => {
            if (!form.value.file.some((item: any) => item.file.name === file.name && item.file.size === file.size)) {
                form.value.file.push({
                    status: 'ready',
                    progress: 0,
                    file: file,
                });
            }
        });
    } else {
        ElMessageBox.alert(`最多上传 ${props.limit} 个文件!`);
    }
};

const handle_entry = (entry: any) => {
    return new Promise((resolve) => {
        if (entry.isFile) {
            entry.file(resolve);
            return;
        }
        const dirReader = entry.createReader();
        dirReader.readEntries(async (entries: any) => {
            resolve(await Promise.all(entries.map(handle_entry)));
        });
    });
};

const del_upload = (index: number) => {
    form.value.file.splice(index, 1);
    file_list.value.splice(index, 1);
};

const handle_paste = (event: any) => {
    const files = event.clipboardData.files;
    const results = [...files].filter((f: any) => validExt(f.name) && validSize(f.size));
    if (results.length + form.value.file.length <= props.limit) {
        results.forEach((file: any) => {
            if (!form.value.file.some((item: any) => item.file.name === file.name && item.file.size === file.size)) {
                form.value.file.push({
                    status: 'ready',
                    progress: 0,
                    file: file,
                });
            } else {
                ElMessageBox.alert(`文件 ${file.name} 已存在!`);
            }
        });
    } else {
        ElMessageBox.alert(`最多上传 ${props.limit} 个文件!`);
    }
};

const submit_event = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate((valid) => {
        if (valid) {
            form.value.file.forEach((item: any) => {
                if (item.status == 'ready') {
                    item.status = 'loading';
                    // 获取分类ID
                    const categoryId = form.value.category_id.length > 0 ? form.value.category_id[form.value.category_id.length - 1] : undefined;
                    
                    uploadFileWithProgress(item.file, categoryId, (percent) => {
                        item.progress = percent;
                    })
                        .then((res) => {
                            ElMessage.success('上传成功');
                            item.status = 'success';
                        })
                        .catch((err) => {
                            console.error('上传失败:', err);
                            item.status = 'error';
                            item.progress = 0;
                            ElMessage.error('上传失败: ' + (err.message || '未知错误'));
                        });
                }
            });
        }
    });
};


// ============ 扫码上传 ============
interface scanFileItem {
    id: number;
    url: string;
    title: string;
    size: number;
    ext: string;
}

const scan_file_list = ref<scanFileItem[]>([]);

// 删除已上传的文件
const del_already_upload = (id: number | undefined, index: number) => {
    if (id != undefined) {
        app?.appContext.config.globalProperties.$common.message_box('删除后不可恢复，确定继续吗?', 'warning').then(() => {
            UploadAPI.delAttachment({ ids: id }).then((res) => {
                ElMessage.success('删除成功!');
                scan_file_list.value.splice(index, 1);
            });
        });
    } else {
        ElMessage.warning('请先选择图片!');
    }
};

// ============ 网络上传 ============
// 提取图片
const extract_images = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate((valid) => {
        if (valid) {
            if (form.value.web_image) {
                const new_data = {
                    type: props.type == 'img' ? 'image' : props.type == 'video' ? 'video' : props.type == 'file' ? 'file' : '',
                    category_id: form.value.category_id[form.value.category_id.length - 1],
                    source: form.value.web_image,
                };
                UploadAPI.getAttachmentCatch(new_data).then((res) => {
                    form.value.web_image = res.data[0].url;
                    ElMessage.success('提取成功!');
                });
            } else {
                ElMessage.warning('请输入地址后再提取!');
            }
        }
    });
};

const emit = defineEmits(['closeAll', 'close']);

// 关闭所有弹窗
const close_all_dialog = () => {
    const new_form = JSON.parse(JSON.stringify(form.value));
    reset_data();
    emit('closeAll', new_form);
};

const file_to_base64 = (file: any) => {
    return URL.createObjectURL(file);
};

const close_dialog = () => {
    reset_data();
    emit('close');
};

const reset_data = () => {
    dialogVisible.value = false;
    form.value = {
        type: 'loc',
        category_id: form.value.category_id,
        file: [],
        qrcode: '',
        web_image: '',
    };
    file_list.value = [];
    scan_file_list.value = [];
    scan_uuid.value = '';
    // 直接检查 timer.value 是否存在（不是 null 或 undefined）
    if (timer.value != null) {
        clearTimeout(timer.value);
    }
    is_mask.value = true;
    timer.value = null; // 清除引用，防止内存泄漏
};
</script>
<style lang="scss" scoped>
@use '../../../styles/variables.scss' as *;
.upload-content {
    height: 57.4rem;
    gap: 4.5rem;
    .table {
        width: 100%;
        height: 30rem;
        .table-header,
        .table-body {
            .progress {
                position: absolute;
                inset: 0;
                width: 0;
                height: 100%;
                background: #f3f9ff;
                transition: width 0.5s linear;
            }
            .table-row {
                position: relative;
                z-index: 1;
                display: flex;
                width: 100%;
                border-bottom: 0.1rem solid #eee;
                color: #999;
                font-size: 1.4rem;
                &.bg-table-rwo-error {
                    background: #f3f9ff;
                }
                .table-cell {
                    flex: 1;
                    padding: 1rem;
                    color: #666;
                    display: flex;
                    align-items: center;
                    word-break: break-all;
                    gap: 1rem;
                    &.loading {
                        color: $cr-primary;
                    }
                    &.success {
                        color: $cr-success;
                    }
                    &.error {
                        color: $cr-error;
                    }
                    .preview-img {
                        width: 2.8rem;
                        height: 2.8rem;
                    }
                    .desc {
                        flex: 1;
                        width: 0;
                    }
                }
                .table-cell-operate {
                    padding: 1rem;
                    width: 5rem;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    gap: 1rem;
                    word-break: break-all;
                }
            }
        }
        .table-body {
            .table-cell,
            .table-cell-operate {
                padding: 1.5rem 1rem !important;
            }
            .table-cell-operate {
                color: $cr-primary;
            }
        }
        .folder-upload {
            background: #fafcff;
            border-radius: 0.2rem;
            border: 0.1rem dashed #afdafa;
            height: 32rem;
            display: flex;
            justify-content: center;
            align-items: center;
            cursor: pointer;
            .active {
                background-color: #eef5ff;
                border-color: #35a9ff;
            }
        }
    }
    .form-item-width {
        width: 33.5rem !important;
    }
}
.upload-2 {
    width: 100%;
    height: 100%;
    :deep(.el-upload) {
        width: inherit;
        height: inherit;
    }
}
</style>
