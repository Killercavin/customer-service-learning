// API Configuration
const API_BASE_URL = 'http://127.0.0.1:8080/api';
const API_ENDPOINTS = {
    topics: `${API_BASE_URL}/topics`,
    search: `${API_BASE_URL}/topics/search`,
    topicById: (id) => `${API_BASE_URL}/topics/${id}`
};

// Global State
let allTopics = [];
let filteredTopics = [];
let currentEditingId = null;
let pendingDeleteId = null;

// DOM Elements
const elements = {
    topicsContainer: document.getElementById('topics-container'),
    searchInput: document.getElementById('search-input'),
    addTopicBtn: document.getElementById('add-topic-btn'),
    refreshBtn: document.getElementById('refresh-btn'),
    modal: document.getElementById('topic-modal'),
    modalTitle: document.getElementById('modal-title'),
    modalClose: document.getElementById('modal-close'),
    topicForm: document.getElementById('topic-form'),
    cancelBtn: document.getElementById('cancel-btn'),
    confirmModal: document.getElementById('confirm-modal'),
    confirmMessage: document.getElementById('confirm-message'),
    confirmYes: document.getElementById('confirm-yes'),
    confirmCancel: document.getElementById('confirm-cancel'),
    toast: document.getElementById('toast'),
    toastMessage: document.getElementById('toast-message'),
    totalTopics: document.getElementById('total-topics'),
    highPriority: document.getElementById('high-priority'),
    avgLength: document.getElementById('avg-length')
};

// Utility Functions
function showToast(message, type = 'success') {
    elements.toastMessage.textContent = message;
    elements.toast.className = `toast ${type} show`;
    setTimeout(() => {
        elements.toast.classList.remove('show');
    }, 3000);
}

function showModal(modal) {
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

function hideModal(modal) {
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function validateTopicData(data) {
    const errors = [];
    
    if (!data.title || data.title.trim().length === 0) {
        errors.push('Title is required');
    }
    
    if (data.title && data.title.trim().length > 200) {
        errors.push('Title must be less than 200 characters');
    }
    
    if (data.description && data.description.length > 1000) {
        errors.push('Description must be less than 1000 characters');
    }
    
    return errors;
}

// API Functions with improved error handling
async function fetchTopics() {
    try {
        showLoadingState();
        const response = await fetch(API_ENDPOINTS.topics, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        if (!response.ok) {
            throw new Error(`Failed to fetch topics (${response.status})`);
        }
        
        const topics = await response.json();
        allTopics = Array.isArray(topics) ? topics : [];
        filteredTopics = [...allTopics];
        
        renderTopics(filteredTopics);
        updateStats(allTopics);
        
    } catch (error) {
        console.error('Fetch Error:', error);
        showErrorState(error.message);
        showToast(`Error loading topics: ${error.message}`, 'error');
    }
}

async function createTopic(topicData) {
    try {
        // Validate data
        const errors = validateTopicData(topicData);
        if (errors.length > 0) {
            throw new Error(errors.join(', '));
        }
        
        const response = await fetch(API_ENDPOINTS.topics, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(topicData)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Failed to create topic (${response.status}): ${errorText}`);
        }
        
        const newTopic = await response.json();
        showToast('✅ Topic created successfully!');
        await fetchTopics(); // Refresh the list
        return newTopic;
        
    } catch (error) {
        console.error('Create Error:', error);
        showToast(`❌ Error creating topic: ${error.message}`, 'error');
        throw error;
    }
}

async function updateTopic(id, topicData) {
    try {
        // Validate data
        const errors = validateTopicData(topicData);
        if (errors.length > 0) {
            throw new Error(errors.join(', '));
        }
        
        const response = await fetch(API_ENDPOINTS.topicById(id), {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(topicData)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Failed to update topic (${response.status}): ${errorText}`);
        }
        
        const updatedTopic = await response.json();
        showToast('✅ Topic updated successfully!');
        await fetchTopics(); // Refresh the list
        return updatedTopic;
        
    } catch (error) {
        console.error('Update Error:', error);
        showToast(`❌ Error updating topic: ${error.message}`, 'error');
        throw error;
    }
}

async function deleteTopic(id) {
    try {
        const response = await fetch(API_ENDPOINTS.topicById(id), {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Failed to delete topic (${response.status}): ${errorText}`);
        }
        
        showToast('✅ Topic deleted successfully!');
        await fetchTopics(); // Refresh the list
        
    } catch (error) {
        console.error('Delete Error:', error);
        showToast(`❌ Error deleting topic: ${error.message}`, 'error');
        throw error;
    }
}

async function searchTopics(query) {
    try {
        if (!query.trim()) {
            filteredTopics = [...allTopics];
            renderTopics(filteredTopics);
            return;
        }
        
        const response = await fetch(`${API_ENDPOINTS.search}?q=${encodeURIComponent(query)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        if (!response.ok) {
            // Fallback to client-side search if server search fails
            console.warn('Server search failed, using client-side search');
            clientSideSearch(query);
            return;
        }
        
        const searchResults = await response.json();
        filteredTopics = Array.isArray(searchResults) ? searchResults : [];
        renderTopics(filteredTopics);
        
    } catch (error) {
        console.error('Search Error:', error);
        // Fallback to client-side search
        clientSideSearch(query);
    }
}

function clientSideSearch(query) {
    const searchTerm = query.toLowerCase();
    filteredTopics = allTopics.filter(topic => {
        const title = (topic.title || '').toLowerCase();
        const description = (topic.description || '').toLowerCase();
        return title.includes(searchTerm) || description.includes(searchTerm);
    });
    renderTopics(filteredTopics);
}

// UI Rendering Functions
function showLoadingState() {
    elements.topicsContainer.innerHTML = `
        <div class="loading">
            <div class="loading-content">
                <div class="loading-spinner"></div>
                <p>Loading your topics...</p>
                <small>Please wait while we fetch your data</small>
            </div>
        </div>
    `;
}

function showErrorState(errorMessage) {
    elements.topicsContainer.innerHTML = `
        <div class="empty-state error-state">
            <div class="error-icon">⚠️</div>
            <h3>Oops! Something went wrong</h3>
            <p class="error-message">${escapeHtml(errorMessage)}</p>
            <div class="error-help">
                <p><strong>What can you do?</strong></p>
                <ul>
                    <li>Check your internet connection</li>
                    <li>Make sure the server is running at: <code>${API_BASE_URL}</code></li>
                    <li>Try refreshing the page</li>
                </ul>
            </div>
            <button class="btn btn-primary" onclick="fetchTopics()">
                🔄 Try Again
            </button>
        </div>
    `;
    
    // Reset stats on error
    elements.totalTopics.textContent = '0';
    elements.highPriority.textContent = '0';
    elements.avgLength.textContent = '0';
}

function renderTopics(topics) {
    if (topics.length === 0) {
        const isSearching = elements.searchInput.value.trim().length > 0;
        elements.topicsContainer.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">${isSearching ? '🔍' : '📝'}</div>
                <h3>${isSearching ? 'No topics match your search' : 'No topics yet'}</h3>
                <p>${isSearching ? 
                    'Try different keywords or clear your search to see all topics' : 
                    'Create your first customer service topic to get started!'
                }</p>
                ${isSearching ? 
                    `<button class="btn btn-secondary" onclick="clearSearch()">Clear Search</button>` :
                    `<button class="btn btn-primary" onclick="openAddModal()">➕ Create First Topic</button>`
                }
            </div>
        `;
        return;
    }

    elements.topicsContainer.innerHTML = topics.map((topic, index) => {
        const topicId = topic.id || index + 1;
        const title = topic.title || 'Untitled Topic';
        const description = topic.description || 'No description available';
        const createdAt = topic.createdAt || topic.created_at;
        
        return `
            <div class="topic-card" data-topic-id="${topicId}">
                <div class="topic-number">${index + 1}</div>
                <h3 class="topic-title" title="${escapeHtml(title)}">${escapeHtml(title)}</h3>
                <p class="topic-description" title="${escapeHtml(description)}">${escapeHtml(description)}</p>
                <div class="topic-meta">
                    <div class="topic-info">
                        <span class="char-count">${description.length} characters</span>
                        ${createdAt ? `<span class="date-created">Created: ${formatDate(createdAt)}</span>` : ''}
                    </div>
                    <div class="topic-actions">
                        <button class="btn btn-edit" onclick="editTopic('${topicId}')" title="Edit this topic">
                            ✏️ Edit
                        </button>
                        <button class="btn btn-delete" onclick="confirmDelete('${topicId}', '${escapeHtml(title)}')" title="Delete this topic">
                            🗑️ Delete
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function updateStats(topics) {
    elements.totalTopics.textContent = topics.length;
    elements.highPriority.textContent = 'N/A';
    
    // Calculate average description length
    const avgLength = topics.length > 0 ? 
        Math.round(topics.reduce((sum, t) => {
            const desc = t.description || '';
            return sum + desc.length;
        }, 0) / topics.length) : 0;
    elements.avgLength.textContent = avgLength;
}

// Event Handlers
function openAddModal() {
    currentEditingId = null;
    elements.modalTitle.textContent = 'Add New Topic';
    elements.topicForm.reset();
    
    // Set focus to title field for better UX
    setTimeout(() => {
        document.getElementById('topic-title').focus();
    }, 100);
    
    showModal(elements.modal);
}

function editTopic(topicId) {
    const topic = allTopics.find(t => String(t.id || allTopics.indexOf(t) + 1) === String(topicId));
    if (!topic) {
        showToast('❌ Topic not found', 'error');
        return;
    }
    
    currentEditingId = topicId;
    elements.modalTitle.textContent = 'Edit Topic';
    
    // Populate form with topic data
    document.getElementById('topic-title').value = topic.title || '';
    document.getElementById('topic-description').value = topic.description || '';
    
    // Set focus to title field
    setTimeout(() => {
        document.getElementById('topic-title').focus();
    }, 100);
    
    showModal(elements.modal);
}

function confirmDelete(topicId, topicTitle) {
    pendingDeleteId = topicId;
    elements.confirmMessage.innerHTML = `
        Are you sure you want to delete the topic:<br>
        <strong>"${escapeHtml(topicTitle)}"</strong><br><br>
        <small style="color: #666;">This action cannot be undone.</small>
    `;
    showModal(elements.confirmModal);
}

function clearSearch() {
    elements.searchInput.value = '';
    filteredTopics = [...allTopics];
    renderTopics(filteredTopics);
    elements.searchInput.focus();
}

// Form handling
async function handleFormSubmit(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const topicData = {
        title: formData.get('title').trim(),
        description: formData.get('description').trim()
    };
    
    // Show loading state on submit button
    const submitButton = document.getElementById('submit-btn');
    const originalText = submitButton.textContent;
    submitButton.textContent = 'Saving...';
    submitButton.disabled = true;
    
    try {
        if (currentEditingId) {
            await updateTopic(currentEditingId, topicData);
        } else {
            await createTopic(topicData);
        }
        
        hideModal(elements.modal);
        elements.topicForm.reset();
        
    } catch (error) {
        // Error already handled in the API functions
        console.error('Form submission error:', error);
    } finally {
        // Restore button state
        submitButton.textContent = originalText;
        submitButton.disabled = false;
    }
}

// Search handling with debounce
let searchTimeout;
function handleSearch(event) {
    const query = event.target.value;
    
    // Clear previous timeout
    clearTimeout(searchTimeout);
    
    // Debounce search to avoid too many API calls
    searchTimeout = setTimeout(() => {
        searchTopics(query);
    }, 300);
}

// Initialize the application
async function initializeApp() {
    try {
        // Add event listeners
        elements.addTopicBtn.addEventListener('click', openAddModal);
        elements.refreshBtn.addEventListener('click', fetchTopics);
        elements.searchInput.addEventListener('input', handleSearch);
        elements.topicForm.addEventListener('submit', handleFormSubmit);
        
        // Modal event listeners
        elements.modalClose.addEventListener('click', () => hideModal(elements.modal));
        elements.cancelBtn.addEventListener('click', () => hideModal(elements.modal));
        
        // Confirmation modal event listeners
        elements.confirmYes.addEventListener('click', async () => {
            if (pendingDeleteId) {
                await deleteTopic(pendingDeleteId);
                pendingDeleteId = null;
            }
            hideModal(elements.confirmModal);
        });
        elements.confirmCancel.addEventListener('click', () => {
            pendingDeleteId = null;
            hideModal(elements.confirmModal);
        });
        
        // Close modals when clicking outside
        window.addEventListener('click', (event) => {
            if (event.target === elements.modal) {
                hideModal(elements.modal);
            }
            if (event.target === elements.confirmModal) {
                hideModal(elements.confirmModal);
                pendingDeleteId = null;
            }
        });
        
        // Keyboard shortcuts
        document.addEventListener('keydown', (event) => {
            // Escape key to close modals
            if (event.key === 'Escape') {
                if (elements.modal.style.display === 'block') {
                    hideModal(elements.modal);
                }
                if (elements.confirmModal.style.display === 'block') {
                    hideModal(elements.confirmModal);
                    pendingDeleteId = null;
                }
            }
            
            // Ctrl/Cmd + K to focus search
            if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
                event.preventDefault();
                elements.searchInput.focus();
            }
            
            // Ctrl/Cmd + N to add new topic
            if ((event.ctrlKey || event.metaKey) && event.key === 'n') {
                event.preventDefault();
                openAddModal();
            }
        });
        
        // Load initial data
        await fetchTopics();
        
        // Show welcome message for new users
        if (allTopics.length === 0) {
            setTimeout(() => {
                showToast('👋 Welcome! Start by creating your first customer service topic.', 'success');
            }, 1000);
        }
        
    } catch (error) {
        console.error('Initialization error:', error);
        showToast('❌ Failed to initialize the application', 'error');
    }
}

// Global functions for onclick handlers
window.editTopic = editTopic;
window.confirmDelete = confirmDelete;
window.clearSearch = clearSearch;
window.openAddModal = openAddModal;
window.fetchTopics = fetchTopics;

// Start the application when DOM is loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeApp);
} else {
    initializeApp();
}