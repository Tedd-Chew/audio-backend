import { useState, useEffect } from 'react'
import axios from 'axios'

function App() {
  const [activeTab, setActiveTab] = useState('product')
  const [products, setProducts] = useState([])
  const [currentProduct, setCurrentProduct] = useState({ name: '', price: 0, description: '', imageUrl: '' })
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(2)
  const [total, setTotal] = useState(0)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [token, setToken] = useState(localStorage.getItem('token'))
  const [loginForm, setLoginForm] = useState({ username: '', password: '' })
  const [aiMessage, setAiMessage] = useState('')
  const [aiReply, setAiReply] = useState('')
  const [aiStatus, setAiStatus] = useState('')
  const [visitorId] = useState(() => {
    let vid = localStorage.getItem('visitorId')
    if (!vid) {
      vid = 'user_' + Math.random().toString(36).substring(2) + Date.now().toString(36)
      localStorage.setItem('visitorId', vid)
    }
    return vid
  })

  const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080',
    headers: {
      'Content-Type': 'application/json'
    }
  })

  axiosInstance.interceptors.request.use(config => {
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  const fetchProducts = async () => {
    try {
      const response = await axiosInstance.get('/api/product/list')
      setProducts(response.data.data)
      setError('')
    } catch (err) {
      setError('获取产品列表失败')
      console.error(err)
    }
  }

  const fetchProductsPage = async () => {
    try {
      const response = await axiosInstance.get(`/api/product/page?pageNum=${pageNum}&pageSize=${pageSize}`)
      setProducts(response.data.data.records)
      setTotal(response.data.data.total)
      setError('')
    } catch (err) {
      setError('分页获取产品失败')
      console.error(err)
    }
  }

  const fetchProductById = async (id) => {
    try {
      const response = await axiosInstance.get(`/api/product/${id}`)
      setCurrentProduct(response.data.data)
      setError('')
    } catch (err) {
      setError('获取产品详情失败')
      console.error(err)
    }
  }

  const addProduct = async () => {
    try {
      await axiosInstance.post('/api/product/add', currentProduct)
      setMessage('添加产品成功')
      setError('')
      fetchProducts()
      setCurrentProduct({ name: '', price: 0, description: '', imageUrl: '' })
    } catch (err) {
      setError('添加产品失败')
      console.error(err)
    }
  }

  const updateProduct = async () => {
    try {
      await axiosInstance.put('/api/product/update', currentProduct)
      setMessage('更新产品成功')
      setError('')
      fetchProducts()
    } catch (err) {
      setError('更新产品失败')
      console.error(err)
    }
  }

  const deleteProduct = async (id) => {
    try {
      await axiosInstance.delete(`/api/product/${id}`)
      setMessage('删除产品成功')
      setError('')
      fetchProducts()
    } catch (err) {
      setError('删除产品失败')
      console.error(err)
    }
  }

  const login = async () => {
    try {
      const response = await axiosInstance.post('/api/user/login', loginForm)
      const newToken = response.data.data
      setToken(newToken)
      localStorage.setItem('token', newToken)
      setMessage('登录成功')
      setError('')
    } catch (err) {
      setError('登录失败')
      console.error(err)
    }
  }

  const logout = async () => {
    try {
      await axiosInstance.post('/api/user/logout')
      setToken('')
      localStorage.removeItem('token')
      setMessage('退出登录成功')
      setError('')
    } catch (err) {
      setError('退出登录失败')
      console.error(err)
    }
  }

  const chatWithAi = async () => {
    if (!aiMessage.trim()) {
      setError('请输入消息')
      return
    }

    try {
      setAiStatus('thinking')
      setAiReply('')
      setError('')

      await axiosInstance.post('/api/ai/chat', {
        message: aiMessage,
        visitorId: visitorId
      })

      pollAiResult()
    } catch (err) {
      setError('AI聊天失败')
      setAiStatus('')
      console.error(err)
    }
  }

  const pollAiResult = () => {
    let attempts = 0
    const maxAttempts = 30

    console.log('开始轮询AI结果，visitorId:', visitorId)

    const interval = setInterval(async () => {
      attempts++
      console.log(`轮询第${attempts}次`)

      try {
        const response = await axiosInstance.get(`/api/ai/result?visitorId=${visitorId}`)
        console.log('完整响应:', response)
        console.log('轮询响应:', response.data)
        
        const { status, reply } = response.data
        console.log('状态:', status, '回复:', reply)

        if (status === 'success' || reply) {
          console.log('AI处理成功，显示回复:', reply)
          setAiReply(reply)
          setAiStatus('')
          clearInterval(interval)
        } else if (attempts >= maxAttempts) {
          console.log('AI处理超时')
          setAiReply('AI处理超时，请稍后重试')
          setAiStatus('')
          clearInterval(interval)
        }
      } catch (err) {
        console.error('轮询失败:', err)
        if (attempts >= maxAttempts) {
          setError('获取AI回复失败')
          setAiStatus('')
          clearInterval(interval)
        }
      }
    }, 1000)
  }

  const testHello = async () => {
    try {
      const response = await axios.get('http://localhost:8080/hello')
      setMessage(`Hello接口测试成功: ${response.data}`)
      setError('')
    } catch (err) {
      setError('Hello接口测试失败')
      console.error(err)
    }
  }

  useEffect(() => {
    if (activeTab === 'product') {
      fetchProductsPage()
    }
  }, [activeTab])

  useEffect(() => {
    if (activeTab === 'product') {
      fetchProductsPage()
    }
  }, [pageNum, pageSize, activeTab])

  return (
    <div className="container">
      <h1>Audio Backend API 测试</h1>

      <nav className="nav">
        <a
          href="#"
          className={activeTab === 'product' ? 'active' : ''}
          onClick={() => setActiveTab('product')}
        >
          产品管理
        </a>
        <a
          href="#"
          className={activeTab === 'user' ? 'active' : ''}
          onClick={() => setActiveTab('user')}
        >
          用户管理
        </a>
        <a
          href="#"
          className={activeTab === 'ai' ? 'active' : ''}
          onClick={() => setActiveTab('ai')}
        >
          AI聊天
        </a>
        <a
          href="#"
          className={activeTab === 'hello' ? 'active' : ''}
          onClick={() => setActiveTab('hello')}
        >
          Hello接口
        </a>
      </nav>

      {message && <div className="success">{message}</div>}
      {error && <div className="error">{error}</div>}

      {activeTab === 'product' && (
        <div className="card">
          <h2>产品管理</h2>

          <div className="form-group">
            <label>产品名称</label>
            <input
              type="text"
              value={currentProduct.name}
              onChange={(e) => setCurrentProduct({...currentProduct, name: e.target.value})}
            />
          </div>
          <div className="form-group">
            <label>价格</label>
            <input
              type="number"
              value={currentProduct.price}
              onChange={(e) => setCurrentProduct({...currentProduct, price: parseFloat(e.target.value)})}
            />
          </div>
          <div className="form-group">
            <label>描述</label>
            <textarea
              value={currentProduct.description}
              onChange={(e) => setCurrentProduct({...currentProduct, description: e.target.value})}
            />
          </div>
          <div className="form-group">
            <label>图片URL</label>
            <input
              type="text"
              value={currentProduct.imageUrl}
              onChange={(e) => setCurrentProduct({...currentProduct, imageUrl: e.target.value})}
            />
          </div>
          <button onClick={addProduct}>添加产品</button>
          <button onClick={updateProduct}>更新产品</button>

          <h3 style={{ color: 'black' }}>产品列表</h3>
          <ul>
            {products.map(product => (
              <li key={product.id} style={{ color: 'black' }}>
                {product.name} - ¥{product.price}
                <button onClick={() => fetchProductById(product.id)}>编辑</button>
                <button onClick={() => deleteProduct(product.id)}>删除</button>
              </li>
            ))}
          </ul>

          <div className="pagination">
            {Array.from({ length: Math.ceil(total / pageSize) }, (_, i) => i + 1).map(page => (
              <li key={page} className={page === pageNum ? 'active' : ''}>
                <a href="#" onClick={() => setPageNum(page)}>{page}</a>
              </li>
            ))}
          </div>
        </div>
      )}

      {activeTab === 'user' && (
        <div className="card">
          <h2>用户管理</h2>

          {!token ? (
            <div>
              <h3>登录</h3>
              <div className="form-group">
                <label>用户名</label>
                <input
                  type="text"
                  value={loginForm.username}
                  onChange={(e) => setLoginForm({...loginForm, username: e.target.value})}
                />
              </div>
              <div className="form-group">
                <label>密码</label>
                <input
                  type="password"
                  value={loginForm.password}
                  onChange={(e) => setLoginForm({...loginForm, password: e.target.value})}
                />
              </div>
              <button onClick={login}>登录</button>
            </div>
          ) : (
            <div>
              <h3>已登录</h3>
              <p>当前Token: {token.substring(0, 20)}...</p>
              <button onClick={logout}>退出登录</button>
            </div>
          )}
        </div>
      )}

      {activeTab === 'ai' && (
        <div className="card">
          <h2>AI聊天</h2>
          <p>访客ID: {visitorId}</p>

          <div className="form-group">
            <label>输入消息</label>
            <textarea
              value={aiMessage}
              onChange={(e) => setAiMessage(e.target.value)}
              rows={4}
              disabled={aiStatus === 'thinking'}
            />
          </div>
          <button onClick={chatWithAi} disabled={aiStatus === 'thinking'}>
            {aiStatus === 'thinking' ? 'AI思考中...' : '发送'}
          </button>

          {aiReply && (
            <div className="form-group">
              <label>AI回复</label>
              <textarea
                value={aiReply}
                readOnly
                rows={4}
              />
            </div>
          )}
        </div>
      )}

      {activeTab === 'hello' && (
        <div className="card">
          <h2>Hello接口测试</h2>
          <button onClick={testHello}>测试Hello接口</button>
        </div>
      )}
    </div>
  )
}

export default App